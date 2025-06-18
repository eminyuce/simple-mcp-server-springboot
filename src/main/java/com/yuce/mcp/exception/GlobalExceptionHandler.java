package com.yuce.mcp.exception;


import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.naming.ServiceUnavailableException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred", ex);
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getDescription(false));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex, WebRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request", request.getDescription(false));
        errorResponse.addDetail("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Invalid request arguments: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request arguments", request.getDescription(false));
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage()));
        errorResponse.addDetail("errors", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(TypeMismatchException ex, WebRequest request) {
        log.warn("Invalid request type: Expected type '{}', but got '{}'", ex.getRequiredType(), ex.getValue());
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request type", request.getDescription(false));
        errorResponse.addDetail("message", String.format("Expected type '%s'", ex.getRequiredType()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, WebRequest request) {
        log.warn("Invalid argument type: Parameter '{}' must be of type '{}'", ex.getName(), ex.getRequiredType().getSimpleName());
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid argument type", request.getDescription(false));
        errorResponse.addDetail("message", String.format("Parameter '%s' must be of type '%s'", ex.getName(), ex.getRequiredType().getSimpleName()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        log.warn("Failed to read request body: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body", request.getDescription(false));
        errorResponse.addDetail("message", "Request body is not readable or is malformed.");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.BAD_REQUEST, "Missing request parameter", request.getDescription(false));
        errorResponse.addDetail("message", String.format("Required parameter '%s' is missing.", ex.getParameterName()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Consider adding specific exception handlers for your custom exceptions
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException ex, WebRequest request) {
        log.error("Service unavailable: {}", ex.getMessage());
        ErrorResponse errorResponse = createErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable", request.getDescription(false));
        errorResponse.addDetail("reason", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    private ErrorResponse createErrorResponse(HttpStatus status, String message, String path) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setPath(path);
        return errorResponse;
    }

    // Define a more structured error response class
    public static class ErrorResponse {
        private int statusCode;
        private String message;
        private LocalDateTime timestamp;
        private String path;
        private Map<String, Object> details = new HashMap<>();

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }

        public void addDetail(String key, Object value) {
            this.details.put(key, value);
        }
    }
}