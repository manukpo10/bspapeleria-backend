package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ModuloRequest {

    @NotBlank
    private String titulo;

    private String descripcion;

    @NotNull
    private Integer orden;

    private List<LeccionRequest> lecciones;
}
