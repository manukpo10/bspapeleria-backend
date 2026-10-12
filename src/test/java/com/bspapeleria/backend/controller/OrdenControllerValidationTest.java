package com.bspapeleria.backend.controller;

import com.bspapeleria.backend.dto.OrdenItemRequest;
import com.bspapeleria.backend.dto.OrdenRequest;
import com.bspapeleria.backend.exception.GlobalExceptionHandler;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks, without a Spring context, the two links that turn a bad order payload into a 400: the controller asks
 * for validation, and the existing handler can render the resulting binding errors (including the nested
 * {@code items[0].cantidad} path) without blowing up.
 */
class OrdenControllerValidationTest {

    private static Method createOrdenMethod() throws NoSuchMethodException {
        return OrdenController.class.getMethod("createOrden", OrdenRequest.class, UserDetails.class);
    }

    @Test
    void createOrden_asksForValidationOfTheRequestBody() throws Exception {
        assertThat(createOrdenMethod().getParameters()[0].isAnnotationPresent(Valid.class)).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    void negativeQuantityAndEmptyItems_areTurnedIntoA400WithTheOffendingFields() throws Exception {
        OrdenItemRequest negativo = new OrdenItemRequest();
        negativo.setTipo("PRODUCTO");
        negativo.setItemId("11");
        negativo.setCantidad(-5);
        OrdenRequest conCantidadNegativa = new OrdenRequest();
        conCantidadNegativa.setItems(List.of(negativo));
        conCantidadNegativa.setMetodoPago("MERCADO_PAGO");
        OrdenRequest sinItems = new OrdenRequest();
        sinItems.setItems(List.of());
        sinItems.setMetodoPago("MERCADO_PAGO");

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            SpringValidatorAdapter validator = new SpringValidatorAdapter(factory.getValidator());
            GlobalExceptionHandler handler = new GlobalExceptionHandler();
            MethodParameter parameter = new MethodParameter(createOrdenMethod(), 0);

            for (OrdenRequest invalid : List.of(conCantidadNegativa, sinItems)) {
                BeanPropertyBindingResult errors = new BeanPropertyBindingResult(invalid, "ordenRequest");
                validator.validate(invalid, errors);

                ResponseEntity<Map<String, Object>> response =
                        handler.handleValidation(new MethodArgumentNotValidException(parameter, errors));

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                Map<String, String> fields = (Map<String, String>) response.getBody().get("errors");
                if (invalid == conCantidadNegativa) {
                    assertThat(fields).containsEntry("items[0].cantidad", "La cantidad debe ser mayor a 0");
                } else {
                    assertThat(fields).containsEntry("items", "El pedido debe incluir al menos un ítem");
                }
            }
        }
    }
}
