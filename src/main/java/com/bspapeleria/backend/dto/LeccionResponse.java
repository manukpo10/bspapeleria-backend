package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeccionResponse {
    private Long id;
    private String titulo;
    private String contenido;
    private String urlVideo;
    private String urlMaterial;
    private Integer orden;
    private Integer duracionMinutos;
    private Boolean esPreview;
}