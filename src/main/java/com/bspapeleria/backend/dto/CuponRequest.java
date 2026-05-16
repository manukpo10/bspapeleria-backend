package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CuponRequest {

    @NotBlank
    private String codigo;

    @NotBlank
    private String tipoDescuento;

    @NotNull @Positive
    private Double valorDescuento;

    private Integer maxUsos;

    private Boolean activo;

    private LocalDateTime fechaVencimiento;

    private Double descuentoMinimo;

    private Double descuentoMaximo;
}