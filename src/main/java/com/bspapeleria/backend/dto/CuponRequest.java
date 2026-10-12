package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Create/update payload. Only presence is checked here; the business rules (percentage between 0 and 100,
 * known discount type, code of at most 50 characters, duplicate code...) live in {@code CuponService} so that every
 * failure comes back as the same {@code {message}} body. {@code maxUsos} is required (an integer of at least 1,
 * checked in the service for the same reason). {@code fechaVencimiento}, {@code descuentoMinimo} (0 or more) and
 * {@code descuentoMaximo} (more than 0) are optional. On update, an omitted {@code activo} keeps the stored value.
 */
@Data
public class CuponRequest {

    @NotBlank(message = "El código del cupón es obligatorio")
    private String codigo;

    @NotBlank(message = "El tipo de descuento es obligatorio")
    private String tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio")
    private Double valorDescuento;

    private Integer maxUsos;

    private Boolean activo;

    private LocalDateTime fechaVencimiento;

    private Double descuentoMinimo;

    private Double descuentoMaximo;
}
