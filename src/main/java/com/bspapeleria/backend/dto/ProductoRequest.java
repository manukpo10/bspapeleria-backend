package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.util.List;

@Data
public class ProductoRequest {
    @NotBlank
    private String nombre;

    private String descripcion;
    private String descripcionCorta;

    @NotNull @Positive
    private Double precio;

    private Double precioComparacion;
    private List<String> imagenes;

    @NotBlank
    private String categoria;

    private List<String> tags;

    @NotNull
    private Integer stock;

    private Boolean esDigital = false;
    private String urlDescarga;
    private Boolean destacado = false;
}