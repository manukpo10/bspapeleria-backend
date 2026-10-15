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

import java.util.Set;

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
}
