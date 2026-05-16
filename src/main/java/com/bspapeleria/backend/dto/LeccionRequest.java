package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeccionRequest {

    @NotBlank
    private String titulo;

    private String contenido;

    private String urlVideo;

    private String urlMaterial;

    @NotNull
    private Integer orden;

    private Integer duracionMinutos;

    private Boolean esPreview = false;
}