package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarritoItemRequest {

    @NotNull
    private String tipo;

    @NotNull
    private String itemId;

    @NotNull
    private Integer cantidad;
}