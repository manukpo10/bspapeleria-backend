package com.bspapeleria.backend.entity;

import com.bspapeleria.backend.dto.PreguntaFrecuente;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Maps the course FAQ list to the nullable {@code text} column {@code cursos.preguntas_frecuentes}, a JSON array of
 * {@code {"pregunta", "respuesta"}} objects. No questions are stored as {@code NULL}.
 *
 * <p>Reading never throws: a row with unreadable JSON loads as an empty list (and logs a warning) so one bad value
 * cannot break the whole course catalogue.
 */
@Slf4j
@Converter
public class PreguntasFrecuentesConverter implements AttributeConverter<List<PreguntaFrecuente>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final TypeReference<List<PreguntaFrecuente>> LIST_TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<PreguntaFrecuente> preguntas) {
        if (preguntas == null || preguntas.isEmpty()) return null;
        try {
            return MAPPER.writeValueAsString(preguntas);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudieron serializar las preguntas frecuentes", e);
        }
    }

    @Override
    public List<PreguntaFrecuente> convertToEntityAttribute(String columna) {
        if (columna == null || columna.isBlank()) return new ArrayList<>();
        try {
            List<PreguntaFrecuente> preguntas = MAPPER.readValue(columna, LIST_TYPE);
            if (preguntas == null) return new ArrayList<>();
            // A stray null element (e.g. "[null]") would blow up later in the mapping, so drop it here
            return preguntas.stream().filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));
        } catch (JsonProcessingException e) {
            // The message can quote a short excerpt of the stored text; FAQs are public, so that is acceptable in the log
            return unreadable(e.getOriginalMessage());
        } catch (RuntimeException e) {
            return unreadable(e.getClass().getSimpleName());
        }
    }

    private static List<PreguntaFrecuente> unreadable(String reason) {
        log.warn("cursos.preguntas_frecuentes tiene un JSON ilegible; se devuelve una lista vacía: {}", reason);
        return new ArrayList<>();
    }
}
