package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.CuponRequest;
import com.bspapeleria.backend.dto.CuponResponse;
import com.bspapeleria.backend.entity.Cupon;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.CuponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CuponService {

    private final CuponRepository cuponRepository;

    @Transactional(readOnly = true)
    public Page<CuponResponse> getAllCupones(Pageable pageable) {
        return cuponRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<Cupon> findCuponByCodigo(String codigo) {
        return cuponRepository.findByCodigoIgnoreCase(codigo);
    }

    @Transactional(readOnly = true)
    public CuponResponse getCuponByCodigo(String codigo) {
        Cupon cupon = cuponRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + codigo));
        return toResponse(cupon);
    }

    @Transactional(readOnly = true)
    public CuponResponse validarCupon(String codigo, Double montoTotal) {
        Cupon cupon = cuponRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + codigo));

        if (!cupon.getActivo()) {
            throw new BadRequestException("El cupón está inactivo");
        }

        if (cupon.getFechaVencimiento() != null && cupon.getFechaVencimiento().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El cupón ha vencido");
        }

        if (cupon.getUsosCount() >= cupon.getMaxUsos()) {
            throw new BadRequestException("El cupón ha alcanzado su límite de usos");
        }

        if (cupon.getDescuentoMinimo() != null && montoTotal < cupon.getDescuentoMinimo()) {
            throw new BadRequestException("El monto mínimo para usar este cupón es: " + cupon.getDescuentoMinimo());
        }

        return toResponse(cupon);
    }

    @Transactional
    public CuponResponse createCupon(CuponRequest request) {
        if (cuponRepository.existsByCodigoIgnoreCase(request.getCodigo())) {
            throw new BadRequestException("Ya existe un cupón con este código");
        }

        Cupon cupon = Cupon.builder()
                .codigo(request.getCodigo().toUpperCase())
                .tipoDescuento(parseTipoDescuento(request.getTipoDescuento()))
                .valorDescuento(request.getValorDescuento())
                .maxUsos(request.getMaxUsos() != null ? request.getMaxUsos() : 1)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .fechaVencimiento(request.getFechaVencimiento())
                .descuentoMinimo(request.getDescuentoMinimo())
                .descuentoMaximo(request.getDescuentoMaximo())
                .build();

        cuponRepository.save(cupon);
        return toResponse(cupon);
    }

    @Transactional
    public CuponResponse updateCupon(Long id, CuponRequest request) {
        Cupon cupon = cuponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + id));

        cupon.setCodigo(request.getCodigo().toUpperCase());
        cupon.setTipoDescuento(parseTipoDescuento(request.getTipoDescuento()));
        cupon.setValorDescuento(request.getValorDescuento());
        if (request.getMaxUsos() != null) cupon.setMaxUsos(request.getMaxUsos());
        if (request.getActivo() != null) cupon.setActivo(request.getActivo());
        if (request.getFechaVencimiento() != null) cupon.setFechaVencimiento(request.getFechaVencimiento());
        if (request.getDescuentoMinimo() != null) cupon.setDescuentoMinimo(request.getDescuentoMinimo());
        if (request.getDescuentoMaximo() != null) cupon.setDescuentoMaximo(request.getDescuentoMaximo());

        cuponRepository.save(cupon);
        return toResponse(cupon);
    }

    @Transactional
    public void deleteCupon(Long id) {
        if (!cuponRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cupón no encontrado: " + id);
        }
        cuponRepository.deleteById(id);
    }

    @Transactional
    public void incrementUsos(String codigo) {
        Cupon cupon = cuponRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón no encontrado: " + codigo));
        cupon.setUsosCount(cupon.getUsosCount() + 1);
        cuponRepository.save(cupon);
    }

    private Cupon.TipoDescuento parseTipoDescuento(String tipo) {
        if (tipo == null) return Cupon.TipoDescuento.PORCENTAJE;
        try {
            return Cupon.TipoDescuento.valueOf(tipo.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return Cupon.TipoDescuento.PORCENTAJE;
        }
    }

    private CuponResponse toResponse(Cupon cupon) {
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
                .build();
    }
}