package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrdenItemRequest {

    @NotNull
    private String tipo;

    @NotNull
    private String itemId;

    @NotNull(message = "Indicá la cantidad de cada ítem")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    private String nombre;
    private String imagenUrl;
    private Double precioUnitario;
}
