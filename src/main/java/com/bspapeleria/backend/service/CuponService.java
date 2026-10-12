package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.CuponRequest;
import com.bspapeleria.backend.dto.CuponResponse;
import com.bspapeleria.backend.entity.Cupon;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.CuponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

@Service
public class CuponService {

    /**
     * The shop's time zone. {@code fechaVencimiento} is a zone-less {@link LocalDateTime} that the admin enters in
     * Argentina time, while the server container runs in UTC, so expiry is judged against this zone's clock.
     */
    public static final ZoneId ZONA_TIENDA = ZoneId.of("America/Argentina/Buenos_Aires");

    private static final String MSG_LIMITE_USOS = "El cupón ya alcanzó su límite de usos";
    private static final String MSG_CODIGO_DUPLICADO = "Ya existe un cupón con ese código";
    private static final int MAX_LONGITUD_CODIGO = 50;

    private final CuponRepository cuponRepository;
    private final Clock clock;

    /** Used by Spring: the clock is the system clock read in the shop's zone (no {@code Clock} bean is needed). */
    @Autowired
    public CuponService(CuponRepository cuponRepository) {
        this(cuponRepository, Clock.system(ZONA_TIENDA));
    }

    /** Lets tests pin "now" without a Spring context. */
    CuponService(CuponRepository cuponRepository, Clock clock) {
        this.cuponRepository = cuponRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<CuponResponse> getAllCupones(Pageable pageable) {
        return cuponRepository.findAll(pageable).map(cupon -> toResponse(cupon, null));
    }

    @Transactional(readOnly = true)
    public CuponResponse getCuponByCodigo(String codigo) {
        Cupon cupon = cuponRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + codigo));
        return toResponse(cupon, null);
    }

    /**
     * Checks whether the coupon can be used for a purchase of {@code montoTotal} (items subtotal) and returns it
     * together with the discount it would give for that amount.
     */
    @Transactional(readOnly = true)
    public CuponResponse validarCupon(String codigo, Double montoTotal) {
        double monto = normalizarMonto(montoTotal);
        Cupon cupon = validarEntidad(codigo, monto);
        return toResponse(cupon, calcularDescuento(cupon, monto));
    }

    /**
     * Validates the coupon for an order whose items add up to {@code subtotal}, counts one use of it and returns
     * the discount to apply. Meant to run inside the order's transaction: any failure here (including losing the
     * race for the last remaining use) rolls the whole order back. A coupon that gives no discount for this order
     * (for example because the items subtotal is 0) is validated but does not consume a use.
     */
    @Transactional
    public double aplicarCupon(String codigo, double subtotal) {
        double monto = normalizarMonto(subtotal);
        Cupon cupon = validarEntidad(codigo, monto);
        double descuento = calcularDescuento(cupon, monto);

        if (descuento <= 0) {
            return 0.0;
        }

        if (cuponRepository.incrementarUsosSiDisponible(cupon.getId()) == 0) {
            throw new BadRequestException(MSG_LIMITE_USOS);
        }
        return descuento;
    }

    /**
     * The one discount rule. Percentage: {@code subtotal * valor / 100}, then capped by {@code descuentoMaximo}
     * when present. Fixed: {@code valor}. Never more than the subtotal, never negative, rounded to cents (HALF_UP).
     * Computed with {@link BigDecimal} so that cents do not drift with binary floating point.
     */
    public double calcularDescuento(Cupon cupon, double subtotal) {
        Cupon.TipoDescuento tipo = cupon.getTipoDescuento();
        Double valorDescuento = cupon.getValorDescuento();
        if (tipo == null || valorDescuento == null || !Double.isFinite(valorDescuento)
                || !Double.isFinite(subtotal) || subtotal <= 0) {
            return 0.0;
        }

        BigDecimal base = BigDecimal.valueOf(subtotal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal valor = BigDecimal.valueOf(valorDescuento);

        BigDecimal descuento = switch (tipo) {
            case PORCENTAJE -> {
                BigDecimal porcentaje = base.multiply(valor).movePointLeft(2);
                Double tope = cupon.getDescuentoMaximo();
                yield tope != null && Double.isFinite(tope) ? porcentaje.min(BigDecimal.valueOf(tope)) : porcentaje;
            }
            case MONTO_FIJO -> valor;
        };

        return descuento.min(base).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Transactional
    public CuponResponse createCupon(CuponRequest request) {
        String codigo = normalizarCodigoParaGuardar(request.getCodigo());
        Cupon.TipoDescuento tipo = validarReglas(request);

        if (cuponRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BadRequestException(MSG_CODIGO_DUPLICADO);
        }

        Cupon cupon = Cupon.builder()
                .codigo(codigo)
                .tipoDescuento(tipo)
                .valorDescuento(request.getValorDescuento())
                .maxUsos(request.getMaxUsos())
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .fechaVencimiento(request.getFechaVencimiento())
                .descuentoMinimo(request.getDescuentoMinimo())
                .descuentoMaximo(request.getDescuentoMaximo())
                .build();

        cuponRepository.save(cupon);
        return toResponse(cupon, null);
    }

    /**
     * Replacement of the editable fields: an optional field ({@code fechaVencimiento}, {@code descuentoMinimo},
     * {@code descuentoMaximo}) that comes as null is cleared, and {@code maxUsos} is required. The one exception is
     * {@code activo}: when omitted it keeps its stored value (on create an omitted {@code activo} means true).
     * {@code usosCount} and {@code fechaCreacion} are never touched.
     */
    @Transactional
    public CuponResponse updateCupon(Long id, CuponRequest request) {
        Cupon cupon = cuponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + id));

        String codigo = normalizarCodigoParaGuardar(request.getCodigo());
        Cupon.TipoDescuento tipo = validarReglas(request);

        if (cuponRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new BadRequestException(MSG_CODIGO_DUPLICADO);
        }

        cupon.setCodigo(codigo);
        cupon.setTipoDescuento(tipo);
        cupon.setValorDescuento(request.getValorDescuento());
        // An omitted activo keeps the stored value, so a client that leaves it out cannot reactivate a coupon.
        if (request.getActivo() != null) {
            cupon.setActivo(request.getActivo());
        }
        cupon.setFechaVencimiento(request.getFechaVencimiento());
        cupon.setMaxUsos(request.getMaxUsos());
        cupon.setDescuentoMinimo(request.getDescuentoMinimo());
        cupon.setDescuentoMaximo(request.getDescuentoMaximo());

        cuponRepository.save(cupon);
        return toResponse(cupon, null);
    }

    @Transactional
    public void deleteCupon(Long id) {
        if (!cuponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cupón no encontrado: " + id);
        }
        cuponRepository.deleteById(id);
    }

    // ------------------------------------------------------------------ rules

    /** Looks the coupon up and runs the usage checks, in order. Shared by validation and order creation. */
    private Cupon validarEntidad(String codigo, double monto) {
        String codigoBuscado = codigo == null ? "" : codigo.trim();
        if (codigoBuscado.isEmpty()) {
            throw new BadRequestException("El código del cupón es obligatorio");
        }

        Cupon cupon = cuponRepository.findByCodigoIgnoreCase(codigoBuscado)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + codigoBuscado));

        if (!Boolean.TRUE.equals(cupon.getActivo())) {
            throw new BadRequestException("El cupón está inactivo");
        }

        if (cupon.getFechaVencimiento() != null
                && cupon.getFechaVencimiento().isBefore(LocalDateTime.now(clock))) {
            throw new BadRequestException("El cupón ha vencido");
        }

        if (cupon.getMaxUsos() != null && usos(cupon) >= cupon.getMaxUsos()) {
            throw new BadRequestException(MSG_LIMITE_USOS);
        }

        if (cupon.getDescuentoMinimo() != null && monto < cupon.getDescuentoMinimo()) {
            throw new BadRequestException(
                    "El monto mínimo de compra para usar este cupón es $" + formatearMonto(cupon.getDescuentoMinimo()));
        }

        return cupon;
    }

    /** Create/update business rules. Returns the parsed discount type. */
    private Cupon.TipoDescuento validarReglas(CuponRequest request) {
        Cupon.TipoDescuento tipo = parseTipoDescuento(request.getTipoDescuento());

        Double valor = request.getValorDescuento();
        if (valor == null || !Double.isFinite(valor)) {
            throw new BadRequestException("El valor del descuento es obligatorio");
        }
        if (tipo == Cupon.TipoDescuento.PORCENTAJE && (valor <= 0 || valor > 100)) {
            throw new BadRequestException("El porcentaje de descuento debe ser mayor a 0 y menor o igual a 100");
        }
        if (tipo == Cupon.TipoDescuento.MONTO_FIJO && valor <= 0) {
            throw new BadRequestException("El monto fijo de descuento debe ser mayor a 0");
        }

        Double minimo = request.getDescuentoMinimo();
        if (minimo != null && (!Double.isFinite(minimo) || minimo < 0)) {
            throw new BadRequestException("El monto mínimo de compra no puede ser negativo");
        }
        // A cap of 0 would make a coupon that always discounts nothing, so when present it must be positive.
        Double maximo = request.getDescuentoMaximo();
        if (maximo != null && (!Double.isFinite(maximo) || maximo <= 0)) {
            throw new BadRequestException("El descuento máximo debe ser mayor a 0");
        }
        // max_usos is NOT NULL in the database and there is no "unlimited" coupon: it must always be given.
        if (request.getMaxUsos() == null) {
            throw new BadRequestException("Indicá la cantidad máxima de usos");
        }
        if (request.getMaxUsos() < 1) {
            throw new BadRequestException("El máximo de usos debe ser al menos 1");
        }
        return tipo;
    }

    private Cupon.TipoDescuento parseTipoDescuento(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new BadRequestException("El tipo de descuento es obligatorio");
        }
        try {
            return Cupon.TipoDescuento.valueOf(tipo.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Tipo de descuento inválido: " + tipo.trim() + ". Valores permitidos: PORCENTAJE, MONTO_FIJO");
        }
    }

    // ---------------------------------------------------------------- helpers

    private String normalizarCodigoParaGuardar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new BadRequestException("El código del cupón es obligatorio");
        }
        String normalizado = codigo.trim().toUpperCase(Locale.ROOT);
        if (normalizado.length() > MAX_LONGITUD_CODIGO) {
            throw new BadRequestException("El código no puede tener más de " + MAX_LONGITUD_CODIGO + " caracteres");
        }
        return normalizado;
    }

    /** Null means 0; anything that is not a finite non-negative number is a client error. Rounded to cents. */
    private double normalizarMonto(Double monto) {
        if (monto == null) {
            return 0.0;
        }
        if (!Double.isFinite(monto) || monto < 0) {
            throw new BadRequestException("El monto total no es válido");
        }
        return BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String formatearMonto(double monto) {
        return BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private int usos(Cupon cupon) {
        return cupon.getUsosCount() != null ? cupon.getUsosCount() : 0;
    }

    private CuponResponse toResponse(Cupon cupon, Double descuentoAplicable) {
        return CuponResponse.builder()
                .id(cupon.getId())
                .codigo(cupon.getCodigo())
                .tipoDescuento(cupon.getTipoDescuento().name())
                .valorDescuento(cupon.getValorDescuento())
                .activo(cupon.getActivo())
                .fechaCreacion(cupon.getFechaCreacion())
                .fechaVencimiento(cupon.getFechaVencimiento())
                .maxUsos(cupon.getMaxUsos())
                .usosCount(cupon.getUsosCount())
                .descuentoMinimo(cupon.getDescuentoMinimo())
                .descuentoMaximo(cupon.getDescuentoMaximo())
                .descuentoAplicable(descuentoAplicable)
                .build();
    }
}
