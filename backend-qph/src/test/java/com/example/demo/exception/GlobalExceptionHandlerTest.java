package com.example.demo.exception;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();


    // =========================================================
    // RESOURCE NOT FOUND
    // =========================================================

    @Test
    void shouldHandleResourceNotFound() {

        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(
                        new ResourceNotFoundException(
                                "No existe"
                        )
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.NOT_FOUND
        );

        assertThat(
                response.getBody()
        ).containsEntry(
                "error",
                "No existe"
        );
    }


    // =========================================================
    // ENDPOINT / RECURSO HTTP NO ENCONTRADO
    // =========================================================

    @Test
    void shouldHandleNoResourceFound() {

        NoResourceFoundException exception =
                new NoResourceFoundException(
                        HttpMethod.GET,
                        "/api/endpoint-inexistente"
                );

        ResponseEntity<Map<String, String>> response =
                handler.handleNoResourceFound(
                        exception
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.NOT_FOUND
        );

        assertThat(
                response.getBody()
        ).isNotNull();

        assertThat(
                response.getBody()
        ).containsEntry(
                "error",
                "Recurso no encontrado"
        );
    }


    // =========================================================
    // BAD CREDENTIALS
    // =========================================================

    @Test
    void shouldHandleBadCredentials() {

        ResponseEntity<Map<String, String>> response =
                handler.handleBadCredentials(
                        new BadCredentialsException(
                                "bad"
                        )
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.UNAUTHORIZED
        );

        assertThat(
                response.getBody()
        ).containsEntry(
                "error",
                "Credenciales inválidas"
        );
    }


    // =========================================================
    // ILLEGAL ARGUMENT
    // =========================================================

    @Test
    void shouldHandleIllegalArgument() {

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(
                        new IllegalArgumentException(
                                "Duplicado"
                        )
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.BAD_REQUEST
        );

        assertThat(
                response.getBody()
        ).containsEntry(
                "error",
                "Duplicado"
        );
    }


    // =========================================================
    // INSUFFICIENT STOCK
    // =========================================================

    @Test
    void shouldHandleInsufficientStock() {

        ResponseEntity<Map<String, String>> response =
                handler.handleInsufficientStock(
                        new InsufficientStockException(
                                "Stock insuficiente"
                        )
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.CONFLICT
        );

        assertThat(
                response.getBody()
        ).containsEntry(
                "error",
                "Stock insuficiente"
        );
    }


    // =========================================================
    // ACCESS DENIED
    // =========================================================

    @Test
    void shouldHandleAccessDenied() {

        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(
                        new AccessDeniedException(
                                "Sin permisos"
                        )
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.FORBIDDEN
        );

        assertThat(
                response.getBody()
        ).containsEntry(
                "error",
                "Sin permisos"
        );
    }


    // =========================================================
    // ERROR GENERAL
    // =========================================================

    @Test
    void shouldHandleUnexpectedExceptionWithoutExposingDetails() {

        ResponseEntity<Map<String, String>> response =
                handler.handleGeneral(
                        new RuntimeException(
                                "database password"
                        )
                );

        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        assertThat(
                response.getBody()
        ).containsEntry(
                "error",
                "Error interno del servidor"
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    @Test
    void shouldHandleBeanValidationErrors() {

        MethodArgumentNotValidException exception =
                mock(
                        MethodArgumentNotValidException.class
                );

        BindingResult bindingResult =
                mock(
                        BindingResult.class
                );


        when(
                exception.getBindingResult()
        ).thenReturn(
                bindingResult
        );


        when(
                bindingResult.getFieldErrors()
        ).thenReturn(
                List.of(

                        new FieldError(
                                "request",
                                "username",
                                "El username es requerido"
                        ),

                        new FieldError(
                                "request",
                                "email",
                                "Email inválido"
                        )
                )
        );


        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(
                        exception
                );


        assertThat(
                response.getStatusCode()
        ).isEqualTo(
                HttpStatus.BAD_REQUEST
        );


        assertThat(
                response.getBody()
        ).containsEntry(
                "error",
                "Validación fallida"
        );


        @SuppressWarnings("unchecked")
        Map<String, String> details =
                (Map<String, String>)
                        response
                                .getBody()
                                .get(
                                        "details"
                                );


        assertThat(
                details
        )
                .containsEntry(
                        "username",
                        "El username es requerido"
                )
                .containsEntry(
                        "email",
                        "Email inválido"
                );
    }
}