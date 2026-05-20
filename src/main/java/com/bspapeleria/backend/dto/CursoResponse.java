package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CursoResponse {
    private Long id;
    private String slug;
    private String titulo;
    private String descripcion;
    private String imagenUrl;
    private Double precio;
    private Double precioComparacion;
    private String nivel;
    private String modalidad;
    private String instructor;
    private Integer duracionHoras;
    private String urlVideoIntro;
    private Boolean activo;
    private Integer estudiantesCount;
    private Double rating;
    private List<String> tags;
    private List<String> materialUrls;
    private List<ModuloResponse> modulos;
    private LocalDateTime createdAt;
}