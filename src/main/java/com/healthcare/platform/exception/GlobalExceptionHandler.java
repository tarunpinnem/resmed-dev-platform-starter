package com.healthcare.platform.exception;

import com.healthcare.platform.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler providing consistent error responses.
 * Demonstrates proper error handling patterns for REST APIs.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} - URI: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        response.setCorrelationId(getCorrelationId(request));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(
            DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource: {} - URI: {}", ex.getMessage(), request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        response.setCorrelationId(getCorrelationId(request));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed - URI: {}", request.getRequestURI());

        List<ApiResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        ApiResponse<Void> response = ApiResponse.error("Validation failed", fieldErrors);
        response.setCorrelationId(getCorrelationId(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch: {} - URI: {}", ex.getMessage(), request.getRequestURI());

        String message = String.format("Invalid value '%s' for parameter '%s'",
                ex.getValue(), ex.getName());
        ApiResponse<Void> response = ApiResponse.error(message);
        response.setCorrelationId(getCorrelationId(request));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed - URI: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error("Invalid credentials");
        response.setCorrelationId(getCorrelationId(request));

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied - URI: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error("Access denied");
        response.setCorrelationId(getCorrelationId(request));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceededException(
            RateLimitExceededException ex, HttpServletRequest request) {
        log.warn("Rate limit exceeded - URI: {}", request.getRequestURI());

        ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
        response.setCorrelationId(getCorrelationId(request));

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error - URI: {}", request.getRequestURI(), ex);

        ApiResponse<Void> response = ApiResponse.error("An unexpected error occurred");
        response.setCorrelationId(getCorrelationId(request));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ApiResponse.FieldError mapFieldError(FieldError fieldError) {
        return ApiResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    private String getCorrelationId(HttpServletRequest request) {
        return (String) request.getAttribute("correlationId");
    }
}
