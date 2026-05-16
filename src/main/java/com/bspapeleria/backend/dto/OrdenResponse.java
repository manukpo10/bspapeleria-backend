package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrdenResponse {
    private Long id;
    private String numeroOrden;
    private String estado;
    private Double subtotal;
    private Double descuento;
    private Double costoEnvio;
    private Double total;
    private String metodoPago;
    private String estadoPago;
    private String mercadoPagoPreferenceId;
    private List<OrdenDetalleResponse> detalles;
    private DireccionEnvioResponse direccionEnvio;
    private String notas;
    private LocalDateTime createdAt;
}