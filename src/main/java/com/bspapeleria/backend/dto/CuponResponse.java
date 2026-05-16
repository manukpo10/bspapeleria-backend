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
}