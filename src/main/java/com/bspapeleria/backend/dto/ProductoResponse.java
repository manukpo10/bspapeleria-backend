package com.bspapeleria.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Long id;
    private String slug;
    private String nombre;
    private String descripcion;
    private String descripcionCorta;
    private Double precio;
    private Double precioComparacion;
    private List<String> imagenes;
    private String categoria;
    private List<String> tags;
    private Integer stock;
    private Boolean esDigital;
    private String urlDescarga;
    private Boolean destacado;
    private Double rating;
    private Integer reviewsCount;
    private LocalDateTime createdAt;
}