package com.wedservice.backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("RESOURCE_NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("UNAUTHORIZED")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        String message = ex instanceof DisabledException
                ? "User account is inactive"
                : "Invalid email or password";

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode("UNAUTHORIZED")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex) {
        log.error("Unhandled exception", ex);

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Internal server error")
                .errorCode("INTERNAL_SERVER_ERROR")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
