package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.*;
import com.bspapeleria.backend.entity.*;
import com.bspapeleria.backend.event.OrdenEstadoActualizadoEvent;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final CursoRepository cursoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ProgresoService progresoService;

    @Transactional(readOnly = true)
    public Page<OrdenResponse> getOrdenesByUsuario(Long usuarioId, Pageable pageable) {
        return ordenRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrdenResponse getOrdenById(Long id, Long usuarioId) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));

        if (!orden.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Orden no encontrada: " + id);
        }

        return toResponse(orden);
    }

    @Transactional(readOnly = true)
    public Page<OrdenResponse> getAllOrdenes(Pageable pageable) {
        return ordenRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrdenResponse> getOrdenesByEstado(String estado, Pageable pageable) {
        Orden.Estado estadoEnum;
        try {
            estadoEnum = Orden.Estado.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + estado);
        }
        return ordenRepository.findByEstadoOrderByFechaCreacionDesc(estadoEnum, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public OrdenResponse createOrden(Long usuarioId, OrdenRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

        String numeroOrden = "BS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Orden orden = Orden.builder()
                .numeroOrden(numeroOrden)
                .usuario(usuario)
                .estado(Orden.Estado.PENDIENTE)
                .metodoPago(parseMetodoPago(request.getMetodoPago()))
                .estadoPago(Orden.EstadoPago.PENDIENTE)
                .notas(request.getNotas())
                .build();

        double subtotal = 0.0;
        List<OrdenDetalle> detalles = new ArrayList<>();

        for (OrdenItemRequest item : request.getItems()) {
            String nombreItem;
            String imagenUrl;
            double precioUnitario;

            if ("PRODUCTO".equals(item.getTipo())) {
                Producto producto = productoRepository.findById(Long.parseLong(item.getItemId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getItemId()));
                nombreItem = producto.getNombre();
                imagenUrl = producto.getImagenes() != null && !producto.getImagenes().isEmpty()
                        ? producto.getImagenes().iterator().next() : null;
                precioUnitario = producto.getPrecio();
            } else {
                Curso curso = cursoRepository.findById(Long.parseLong(item.getItemId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + item.getItemId()));
                nombreItem = curso.getTitulo();
                imagenUrl = curso.getImagenUrl();
                precioUnitario = curso.getPrecio();
            }

            double itemSubtotal = precioUnitario * item.getCantidad();
            subtotal += itemSubtotal;

            OrdenDetalle detalle = OrdenDetalle.builder()
                    .orden(orden)
                    .tipoItem(OrdenDetalle.TipoItem.valueOf(item.getTipo()))
                    .itemId(item.getItemId())
                    .nombre(nombreItem)
                    .imagenUrl(imagenUrl)
                    .cantidad(item.getCantidad())
                    .precioUnitario(precioUnitario)
                    .subtotal(itemSubtotal)
                    .build();

            detalles.add(detalle);
        }

        boolean tieneProductosFisicos = detalles.stream()
                .anyMatch(d -> d.getTipoItem() == OrdenDetalle.TipoItem.PRODUCTO);

        double costoEnvio = tieneProductosFisicos ? 1500.0 : 0.0;
        double descuento = 0.0;

        if (request.getCodigoCupon() != null && !request.getCodigoCupon().isEmpty()) {
            descuento = calcularDescuento(subtotal, request.getCodigoCupon());
        }

        orden.setDetalles(detalles);
        orden.setSubtotal(subtotal);
        orden.setDescuento(descuento);
        orden.setCostoEnvio(costoEnvio);
        orden.setTotal(subtotal - descuento + costoEnvio);

        if (request.getDireccionEnvio() != null) {
            DireccionEnvioRequest dirReq = request.getDireccionEnvio();
            orden.setDireccionEnvio(Orden.DireccionEnvio.builder()
                    .calle(dirReq.getCalle())
                    .numero(dirReq.getNumero())
                    .piso(dirReq.getPiso())
                    .departamento(dirReq.getDepartamento())
                    .ciudad(dirReq.getCiudad())
                    .provincia(dirReq.getProvincia())
                    .codigoPostal(dirReq.getCodigoPostal())
                    .telefono(dirReq.getTelefono())
                    .instrucciones(dirReq.getInstrucciones())
                    .build());
        }

        ordenRepository.save(orden);
        return toResponse(orden);
    }

    @Transactional
    public OrdenResponse updateOrdenEstado(Long id, String estado) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));

        Orden.Estado estadoAnterior = orden.getEstado();
        String estadoAnteriorStr = estadoAnterior.name();

        Orden.Estado estadoEnum;
        try {
            estadoEnum = Orden.Estado.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + estado);
        }

        orden.setEstado(estadoEnum);
        ordenRepository.save(orden);

        eventPublisher.publishEvent(new OrdenEstadoActualizadoEvent(this, orden, estadoAnteriorStr));

        return toResponse(orden);
    }

    @Transactional
    public void actualizarEstadoPagoMercadoPago(String preferenceId, String paymentId, String status) {
        List<Orden> ordenes = ordenRepository.findAll();
        for (Orden orden : ordenes) {
            if (orden.getMercadoPagoPreferenceId() != null &&
                orden.getMercadoPagoPreferenceId().equals(preferenceId)) {

                orden.setMercadoPagoPaymentId(paymentId);

                if ("approved".equalsIgnoreCase(status)) {
                    orden.setEstadoPago(Orden.EstadoPago.APROBADO);
                    orden.setEstado(Orden.Estado.CONFIRMADA);
                } else if ("rejected".equalsIgnoreCase(status)) {
                    orden.setEstadoPago(Orden.EstadoPago.RECHAZADO);
                } else if ("pending".equalsIgnoreCase(status)) {
                    orden.setEstadoPago(Orden.EstadoPago.PENDIENTE);
                } else if ("refunded".equalsIgnoreCase(status)) {
                    orden.setEstadoPago(Orden.EstadoPago.REEMBOLSADO);
                    orden.setEstado(Orden.Estado.CANCELADA);
                }

                ordenRepository.save(orden);
                return;
            }
        }
    }

    private double calcularDescuento(double subtotal, String codigoCupon) {
        if ("DESCUENTO10".equalsIgnoreCase(codigoCupon)) {
            return subtotal * 0.10;
        }
        return 0.0;
    }

    private Orden.MetodoPago parseMetodoPago(String metodo) {
        if (metodo == null || metodo.isEmpty()) return Orden.MetodoPago.MERCADO_PAGO;
        try {
            return Orden.MetodoPago.valueOf(metodo.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Orden.MetodoPago.MERCADO_PAGO;
        }
    }

    private OrdenResponse toResponse(Orden orden) {
        List<OrdenDetalleResponse> detalles = null;
        if (orden.getDetalles() != null && !orden.getDetalles().isEmpty()) {
            detalles = orden.getDetalles().stream()
                    .map(d -> OrdenDetalleResponse.builder()
                            .id(d.getId())
                            .tipoItem(d.getTipoItem().name())
                            .itemId(d.getItemId())
                            .nombre(d.getNombre())
                            .imagenUrl(d.getImagenUrl())
                            .cantidad(d.getCantidad())
                            .precioUnitario(d.getPrecioUnitario())
                            .subtotal(d.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
        }

        DireccionEnvioResponse direccion = null;
        if (orden.getDireccionEnvio() != null) {
            Orden.DireccionEnvio de = orden.getDireccionEnvio();
            direccion = DireccionEnvioResponse.builder()
                    .calle(de.getCalle())
                    .numero(de.getNumero())
                    .piso(de.getPiso())
                    .departamento(de.getDepartamento())
                    .ciudad(de.getCiudad())
                    .provincia(de.getProvincia())
                    .codigoPostal(de.getCodigoPostal())
                    .telefono(de.getTelefono())
                    .instrucciones(de.getInstrucciones())
                    .build();
        }

        return OrdenResponse.builder()
                .id(orden.getId())
                .numeroOrden(orden.getNumeroOrden())
                .estado(orden.getEstado().name())
                .subtotal(orden.getSubtotal())
                .descuento(orden.getDescuento())
                .costoEnvio(orden.getCostoEnvio())
                .total(orden.getTotal())
                .metodoPago(orden.getMetodoPago().name())
                .estadoPago(orden.getEstadoPago().name())
                .mercadoPagoPreferenceId(orden.getMercadoPagoPreferenceId())
                .detalles(detalles)
                .direccionEnvio(direccion)
                .notas(orden.getNotas())
                .createdAt(orden.getFechaCreacion())
                .build();
    }
}