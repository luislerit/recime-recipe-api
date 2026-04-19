package com.recime.recipeapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRecipeNotFound_returns404WithProblemDetail() {
        ResponseEntity<ProblemDetail> response = handler.handleRecipeNotFound(
                new RecipeNotFoundException("Recipe not found"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getTitle()).isEqualTo("Not Found");
        assertThat(body.getDetail()).isEqualTo("Recipe not found");
    }

    @Test
    void handleValidation_returns400WithFieldErrorsProperty() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Dummy(), "dummy");
        bindingResult.addError(new FieldError("dummy", "title", "must not be blank"));
        MethodParameter parameter = new MethodParameter(Dummy.class.getMethod("noop"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ProblemDetail> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Validation Failed");
        assertThat(body.getDetail()).contains("title").contains("must not be blank");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> errors = (List<Map<String, String>>) body.getProperties().get("errors");
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).containsEntry("field", "title").containsEntry("message", "must not be blank");
    }

    @Test
    void handleMissingHeader_returns400() throws Exception {
        Method method = Dummy.class.getMethod("noop");
        MethodParameter parameter = new MethodParameter(method, -1);
        MissingRequestHeaderException ex = new MissingRequestHeaderException("X-User-Id", parameter);

        ResponseEntity<ProblemDetail> response = handler.handleMissingHeader(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getDetail()).contains("X-User-Id");
    }

    @Test
    void handleDataIntegrity_returns409Conflict() {
        ResponseEntity<ProblemDetail> response = handler.handleDataIntegrity(
                new DataIntegrityViolationException("duplicate key"));

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Conflict");
        assertThat(body.getDetail()).isEqualTo("Request conflicts with existing data");
    }

    @Test
    void handleGeneric_returns500WithGenericMessage() {
        ResponseEntity<ProblemDetail> response = handler.handleGeneric(new RuntimeException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Internal Server Error");
        assertThat(body.getDetail()).isEqualTo("An unexpected error occurred");
    }

    static class Dummy {
        public void noop() {}
    }
}
