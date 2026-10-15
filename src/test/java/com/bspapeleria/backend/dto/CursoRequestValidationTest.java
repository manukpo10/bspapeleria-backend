package com.bspapeleria.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation on the course payload, run with the plain Jakarta validator (no Spring context). The short
 * description is optional, but it lives in a {@code varchar(500)} column, so anything longer must be rejected
 * with a 400 instead of reaching the database.
 */
class CursoRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static CursoRequest request(String descripcionCorta) {
        CursoRequest request = new CursoRequest();
        request.setTitulo("La Magia del Candy Bar");
        request.setPrecio(40000.0);
        request.setNivel("principiante");
        request.setModalidad("video");
        request.setDescripcionCorta(descripcionCorta);
        return request;
    }

    @Test
    void shortDescriptionOfExactlyTheColumnLimit_isValid() {
        assertThat(validator.validate(request("a".repeat(500)))).isEmpty();
    }

    @Test
    void shortDescriptionOneCharacterOverTheColumnLimit_isRejectedWithASpanishMessage() {
        Set<ConstraintViolation<CursoRequest>> violations = validator.validate(request("a".repeat(501)));

        assertThat(violations).hasSize(1);
        ConstraintViolation<CursoRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath()).hasToString("descripcionCorta");
        assertThat(violation.getMessage()).isEqualTo("La descripción corta no puede superar los 500 caracteres");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "Una sola oración."})
    void shortDescriptionIsOptional(String value) {
        assertThat(validator.validate(request(value))).isEmpty();
    }

    // The syllabus intro lives in the same kind of varchar(500) column, so it gets the same limit.

    private static CursoRequest requestWithSyllabusIntro(String introduccionTemario) {
        CursoRequest request = request(null);
        request.setIntroduccionTemario(introduccionTemario);
        return request;
    }

    @Test
    void syllabusIntroOfExactlyTheColumnLimit_isValid() {
        assertThat(validator.validate(requestWithSyllabusIntro("a".repeat(500)))).isEmpty();
    }

    @Test
    void syllabusIntroOneCharacterOverTheColumnLimit_isRejectedWithASpanishMessage() {
        Set<ConstraintViolation<CursoRequest>> violations =
                validator.validate(requestWithSyllabusIntro("a".repeat(501)));

        assertThat(violations).hasSize(1);
        ConstraintViolation<CursoRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath()).hasToString("introduccionTemario");
        assertThat(violation.getMessage()).isEqualTo("La introducción del temario no puede superar los 500 caracteres");
    }

    // The FAQ list is stored as JSON in a text column, so the payload is bounded here instead: at most 20 items,
    // each with a question of up to 300 characters and an answer of up to 2000.

    private static CursoRequest requestWithFaqs(List<PreguntaFrecuente> preguntas) {
        CursoRequest request = request(null);
        request.setPreguntasFrecuentes(preguntas);
        return request;
    }

    private static List<PreguntaFrecuente> faqs(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new PreguntaFrecuente("¿Pregunta " + i + "?", "Respuesta " + i))
                .toList();
    }

    private static ConstraintViolation<CursoRequest> onlyViolationOf(CursoRequest request) {
        Set<ConstraintViolation<CursoRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        return violations.iterator().next();
    }

    @Test
    void faqListIsOptional() {
        assertThat(validator.validate(requestWithFaqs(null))).isEmpty();
        assertThat(validator.validate(requestWithFaqs(List.of()))).isEmpty();
    }

    @Test
    void faqListOfExactlyTwentyItems_isValid() {
        assertThat(validator.validate(requestWithFaqs(faqs(20)))).isEmpty();
    }

    @Test
    void faqListOfTwentyOneItems_isRejectedWithASpanishMessage() {
        ConstraintViolation<CursoRequest> violation = onlyViolationOf(requestWithFaqs(faqs(21)));

        assertThat(violation.getPropertyPath()).hasToString("preguntasFrecuentes");
        assertThat(violation.getMessage()).isEqualTo("Un curso admite hasta 20 preguntas frecuentes");
    }

    @Test
    void faqWithABlankQuestion_isRejectedAndPointsAtTheItem() {
        ConstraintViolation<CursoRequest> violation =
                onlyViolationOf(requestWithFaqs(List.of(new PreguntaFrecuente("¿Ok?", "Ok"), new PreguntaFrecuente("   ", "Respuesta"))));

        assertThat(violation.getPropertyPath()).hasToString("preguntasFrecuentes[1].pregunta");
        assertThat(violation.getMessage()).isEqualTo("La pregunta es obligatoria");
    }

    @Test
    void faqWithABlankAnswer_isRejectedAndPointsAtTheItem() {
        ConstraintViolation<CursoRequest> violation =
                onlyViolationOf(requestWithFaqs(List.of(new PreguntaFrecuente("¿Pregunta?", ""))));

        assertThat(violation.getPropertyPath()).hasToString("preguntasFrecuentes[0].respuesta");
        assertThat(violation.getMessage()).isEqualTo("La respuesta es obligatoria");
    }

    @Test
    void faqQuestionAndAnswerAtTheirLimits_areValid() {
        assertThat(validator.validate(requestWithFaqs(List.of(
                new PreguntaFrecuente("a".repeat(300), "b".repeat(2000)))))).isEmpty();
    }

    @Test
    void faqQuestionOverTheLimit_isRejectedWithASpanishMessage() {
        ConstraintViolation<CursoRequest> violation =
                onlyViolationOf(requestWithFaqs(List.of(new PreguntaFrecuente("a".repeat(301), "Respuesta"))));

        assertThat(violation.getPropertyPath()).hasToString("preguntasFrecuentes[0].pregunta");
        assertThat(violation.getMessage()).isEqualTo("La pregunta no puede superar los 300 caracteres");
    }

    @Test
    void faqAnswerOverTheLimit_isRejectedWithASpanishMessage() {
        ConstraintViolation<CursoRequest> violation =
                onlyViolationOf(requestWithFaqs(List.of(new PreguntaFrecuente("¿Pregunta?", "b".repeat(2001)))));

        assertThat(violation.getPropertyPath()).hasToString("preguntasFrecuentes[0].respuesta");
        assertThat(violation.getMessage()).isEqualTo("La respuesta no puede superar los 2000 caracteres");
    }
}
