package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrdenRequest {

    @NotNull
    private List<OrdenItemRequest> items;

    private String codigoCupon;

    private DireccionEnvioRequest direccionEnvio;

    @NotBlank
    private String metodoPago;

    private String notas;
}