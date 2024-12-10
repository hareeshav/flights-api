package com.schiphol.flights.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles validation errors caused by @Valid annotations (MethodArgumentNotValidException).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), getValidationErrorMessage(error));
        }

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handle deserialization failures, especially when date/time fields fail to parse.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, String> errorResponse = new HashMap<>();

        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {

            String path = invalidFormatException.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .reduce((f1, f2) -> f2) // Extract field name
                    .orElse("");

            String errorMessage = "Invalid format. Date and time must follow the format: yyyy-MM-dd'T'HH:mm:ss";
            if ("startScheduleDateTime".equals(path)) {
                errorResponse.put("startScheduleDateTime", errorMessage);
            } else if ("endScheduleDateTime".equals(path)) {
                errorResponse.put("endScheduleDateTime", errorMessage);
            }
        } else {
            errorResponse.put("error", "Malformed JSON request.");
        }

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle all unexpected exceptions with a generic fallback response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid request.");
        errorResponse.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Utility function to define field error messages for MethodArgumentNotValidException.
     */
    private String getValidationErrorMessage(FieldError error) {
        if ("startScheduleDateTime".equals(error.getField()) || "endScheduleDateTime".equals(error.getField())) {
            return "Invalid date/time format. Expected: yyyy-MM-dd'T'HH:mm:ss";
        }
        return error.getDefaultMessage();
    }
}
