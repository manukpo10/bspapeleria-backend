package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class CursoRequest {

    @NotBlank
    private String titulo;

    private String descripcion;

    @Size(max = 500, message = "La descripción corta no puede superar los 500 caracteres")
    private String descripcionCorta;

    private String imagenUrl;

    @NotNull @Positive
    private Double precio;

    private Double precioComparacion;

    @NotBlank
    private String nivel;

    @NotBlank
    private String modalidad;

    private String instructor;

    private Integer duracionHoras;

    private String urlVideoIntro;

    private List<String> tags;

    private List<String> materialUrls;

    private List<ModuloRequest> modulos;
}