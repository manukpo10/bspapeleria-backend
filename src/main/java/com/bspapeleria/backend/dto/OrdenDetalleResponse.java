package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrdenDetalleResponse {
    private Long id;
    private String tipoItem;
    private String itemId;
    private String nombre;
    private String imagenUrl;
    private Integer cantidad;
    private Double precioUnitario;
    private Double subtotal;
}