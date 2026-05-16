package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValoracionRequest {

    @NotBlank
    private String entidadTipo;

    @NotNull
    private Long entidadId;

    @NotNull
    private Integer calificacion;

    private String comentario;
}