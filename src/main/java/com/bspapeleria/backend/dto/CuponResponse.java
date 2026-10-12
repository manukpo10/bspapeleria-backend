package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CuponResponse {
    private Long id;
    private String codigo;
    private String tipoDescuento;
    private Double valorDescuento;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaVencimiento;
    private Integer maxUsos;
    private Integer usosCount;
    private Double descuentoMinimo;
    private Double descuentoMaximo;

    /**
     * Discount this coupon gives for the amount sent to {@code GET /api/cupones/validar}.
     * Null in list, create and update responses, where there is no amount to apply it to.
     */
    private Double descuentoAplicable;
}
