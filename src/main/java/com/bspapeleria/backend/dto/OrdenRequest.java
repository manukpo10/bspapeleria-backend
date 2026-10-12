package com.bspapeleria.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrdenRequest {

    @NotEmpty(message = "El pedido debe incluir al menos un ítem")
    @Valid
    private List<OrdenItemRequest> items;

    private String codigoCupon;

    private DireccionEnvioRequest direccionEnvio;

    @NotBlank
    private String metodoPago;

    private String notas;
}
