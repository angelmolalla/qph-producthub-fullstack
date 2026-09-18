package com.example.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            ResourceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        for (FieldError fieldError :
                ex.getBindingResult().getFieldErrors()) {

            errors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error",
                        "Validación fallida",
                        "details",
                        errors
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            BadCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error",
                        "Credenciales inválidas"
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(
            Exception ex) {

        log.error(
                "Error no controlado",
                ex
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error",
                        "Error interno del servidor"
                ));
    }

    @ExceptionHandler(
            InsufficientStockException.class
    )
    public ResponseEntity<Map<String, String>>
    handleInsufficientStock(
            InsufficientStockException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        Map.of(
                                "error",
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(
            AccessDeniedException.class
    )
    public ResponseEntity<Map<String, String>>
    handleAccessDenied(
            AccessDeniedException ex) {

        return ResponseEntity
                .status(
                        HttpStatus.FORBIDDEN
                )
                .body(
                        Map.of(
                                "error",
                                ex.getMessage()
                        )
                );
    }
}