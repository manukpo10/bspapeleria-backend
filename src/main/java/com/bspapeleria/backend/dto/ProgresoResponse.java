package com.bspapeleria.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProgresoResponse {
    private Long id;
    private Long cursoId;
    private String tituloCurso;
    private List<Long> leccionesCompletadas;
    private Integer porcentajeProgreso;
    private Integer leccionActualId;
    private LocalDateTime ultimaActividad;
    private LocalDateTime fechaInscripcion;
    private Boolean completado;
    private LocalDateTime fechaCompletado;
    private Boolean certificadoDesbloqueado;
}