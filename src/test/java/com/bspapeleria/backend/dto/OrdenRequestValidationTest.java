package com.bspapeleria.backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bean Validation on the order payload, run with the plain Jakarta validator (no Spring context). The point is
 * money: a zero or negative quantity must not be able to lower the subtotal, and an order needs items.
 */
class OrdenRequestValidationTest {

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

    private static OrdenItemRequest item(int cantidad) {
        OrdenItemRequest item = new OrdenItemRequest();
        item.setTipo("PRODUCTO");
        item.setItemId("11");
        item.setCantidad(cantidad);
        return item;
    }

    private static OrdenRequest request(List<OrdenItemRequest> items) {
        OrdenRequest request = new OrdenRequest();
        request.setItems(items);
        request.setMetodoPago("MERCADO_PAGO");
        return request;
    }

    private static ConstraintViolation<OrdenRequest> only(Set<ConstraintViolation<OrdenRequest>> violations) {
        assertThat(violations).hasSize(1);
        return violations.iterator().next();
    }

    @Test
    void validRequest_hasNoViolations() {
        assertThat(validator.validate(request(List.of(item(1), item(25))))).isEmpty();
    }

    @Test
    void nullItems_areRejected() {
        ConstraintViolation<OrdenRequest> violation = only(validator.validate(request(null)));

        assertThat(violation.getPropertyPath()).hasToString("items");
        assertThat(violation.getMessage()).isEqualTo("El pedido debe incluir al menos un ítem");
    }

    @Test
    void emptyItems_areRejected() {
        ConstraintViolation<OrdenRequest> violation = only(validator.validate(request(List.of())));

        assertThat(violation.getPropertyPath()).hasToString("items");
        assertThat(violation.getMessage()).isEqualTo("El pedido debe incluir al menos un ítem");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void zeroOrNegativeQuantity_isRejected_insideTheItemsList(int cantidad) {
        ConstraintViolation<OrdenRequest> violation =
                only(validator.validate(request(List.of(item(2), item(cantidad)))));

        assertThat(violation.getPropertyPath()).hasToString("items[1].cantidad");
        assertThat(violation.getMessage()).isEqualTo("La cantidad debe ser mayor a 0");
    }

    @Test
    void missingQuantity_isRejected() {
        OrdenItemRequest sinCantidad = item(1);
        sinCantidad.setCantidad(null);

        ConstraintViolation<OrdenRequest> violation = only(validator.validate(request(List.of(sinCantidad))));

        assertThat(violation.getPropertyPath()).hasToString("items[0].cantidad");
        assertThat(violation.getMessage()).isEqualTo("Indicá la cantidad de cada ítem");
    }
}
