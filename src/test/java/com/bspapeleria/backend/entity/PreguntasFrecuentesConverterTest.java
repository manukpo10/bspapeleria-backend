package com.bspapeleria.backend.entity;

import com.bspapeleria.backend.dto.PreguntaFrecuente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Unit tests for the converter behind {@code cursos.preguntas_frecuentes}, a nullable {@code text} column that holds a
 * JSON array of {@code {"pregunta", "respuesta"}} objects. No Spring context and no database.
 *
 * <p>The read side must never throw: one malformed row cannot be allowed to break the whole course catalogue.
 */
class PreguntasFrecuentesConverterTest {

    private final PreguntasFrecuentesConverter converter = new PreguntasFrecuentesConverter();

    // ---------------------------------------------------------------- write

    @Test
    void toDatabaseColumn_writesAnArrayWithTheWireKeysAndKeepsAccentsReadable() {
        String json = converter.convertToDatabaseColumn(List.of(new PreguntaFrecuente("¿Necesito experiencia?", "¡No!")));

        assertThat(json).isEqualTo("[{\"pregunta\":\"¿Necesito experiencia?\",\"respuesta\":\"¡No!\"}]");
    }

    @Test
    void toDatabaseColumn_storesNullForAnEmptyList() {
        assertThat(converter.convertToDatabaseColumn(List.of())).isNull();
    }

    @Test
    void toDatabaseColumn_storesNullForANullList() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    // ---------------------------------------------------------------- read

    @Test
    void roundTrip_keepsEveryQuestionInOrder() {
        List<PreguntaFrecuente> original = List.of(
                new PreguntaFrecuente("Primera", "Respuesta uno"),
                new PreguntaFrecuente("¿Segunda?", "Con \"comillas\", salto\nde línea y ñandú"),
                new PreguntaFrecuente("Tercera", "¡Listo!"));

        List<PreguntaFrecuente> back = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original));

        assertThat(back).containsExactlyElementsOf(original);
    }

    @Test
    void toEntityAttribute_readsTheExactShapeAlreadyStoredInProduction() {
        String stored = "[{\"pregunta\": \"¿Cuánto tiempo tengo acceso al curso?\", "
                + "\"respuesta\": \"¡De por vida! Podés verlo cuando quieras.\"}, "
                + "{\"pregunta\": \"¿Necesito PowerPoint?\", \"respuesta\": \"Sí, cualquier versión.\"}]";

        List<PreguntaFrecuente> read = converter.convertToEntityAttribute(stored);

        assertThat(read).containsExactly(
                new PreguntaFrecuente("¿Cuánto tiempo tengo acceso al curso?", "¡De por vida! Podés verlo cuando quieras."),
                new PreguntaFrecuente("¿Necesito PowerPoint?", "Sí, cualquier versión."));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "\t\n", "[]"})
    void toEntityAttribute_returnsAnEmptyListForANullBlankOrEmptyColumn(String column) {
        assertThat(converter.convertToEntityAttribute(column)).isNotNull().isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "esto no es json",
            "[{\"pregunta\": \"cortada\"",
            "{\"pregunta\": \"objeto\", \"respuesta\": \"en lugar de un arreglo\"}",
            "[1, 2, 3]",
            "null"})
    void toEntityAttribute_returnsAnEmptyListAndDoesNotThrowForUnreadableJson(String column) {
        assertThatCode(() -> converter.convertToEntityAttribute(column)).doesNotThrowAnyException();
        assertThat(converter.convertToEntityAttribute(column)).isNotNull().isEmpty();
    }

    @Test
    void toEntityAttribute_ignoresUnknownPropertiesSoANewerRowStillLoads() {
        List<PreguntaFrecuente> read = converter.convertToEntityAttribute(
                "[{\"pregunta\": \"P\", \"respuesta\": \"R\", \"orden\": 3}]");

        assertThat(read).containsExactly(new PreguntaFrecuente("P", "R"));
    }
}
