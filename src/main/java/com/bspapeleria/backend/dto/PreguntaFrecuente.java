package com.bspapeleria.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One frequently asked question of a course. The same shape travels in the API ({@code CursoRequest} and
 * {@code CursoResponse}) and is stored as-is inside the JSON array of {@code cursos.preguntas_frecuentes}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaFrecuente {

    @NotBlank(message = "La pregunta es obligatoria")
    @Size(max = 300, message = "La pregunta no puede superar los 300 caracteres")
    private String pregunta;

    @NotBlank(message = "La respuesta es obligatoria")
    @Size(max = 2000, message = "La respuesta no puede superar los 2000 caracteres")
    private String respuesta;
}
