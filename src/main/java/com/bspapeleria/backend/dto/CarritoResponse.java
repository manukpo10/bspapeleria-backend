package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CarritoResponse {
    private Long id;
    private List<CarritoItemResponse> items;
    private Double subtotal;
    private Double descuento;
    private Double total;
    private LocalDateTime fechaActualizacion;

    @Data
    @Builder
    public static class CarritoItemResponse {
        private Long id;
        private String tipoItem;
        private String itemId;
        private String nombre;
        private String imagenUrl;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}