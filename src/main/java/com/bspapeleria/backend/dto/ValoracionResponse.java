package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ValoracionResponse {
    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private String entidadTipo;
    private Long entidadId;
    private Integer calificacion;
    private String comentario;
    private LocalDateTime createdAt;
}