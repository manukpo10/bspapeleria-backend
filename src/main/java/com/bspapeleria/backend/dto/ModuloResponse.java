package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ModuloResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private Integer orden;
    private List<LeccionResponse> lecciones;
}
