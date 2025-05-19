package com.inghub.loanapi.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * Handle all custom exceptions with a unified method.
     */
    @ExceptionHandler({
            CustomerNotFoundException.class,
            InsufficientCreditException.class,
            InvalidLoanParameterException.class,
            LoanAlreadyPaidException.class,
            PaymentException.class
    })
    public ResponseEntity<ErrorResponse> handleCustomExceptions(RuntimeException ex, WebRequest request) {
        log.error("Exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        HttpStatus status = determineStatus(ex);
        return buildErrorResponse(ex.getMessage(), status, request);
    }

    /**
     * Handle validation exceptions (DTO validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage(),
                        (existing, replacement) -> existing // Prevents duplicate keys
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handle all uncaught exceptions (fallback).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error: ", ex);
        return buildErrorResponse("An unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    /**
     * Build a standard error response for all exceptions.
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                message,
                request.getDescription(false),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Determine the appropriate HTTP status for each custom exception.
     */
    private HttpStatus determineStatus(RuntimeException ex) {
        if (ex instanceof CustomerNotFoundException) return HttpStatus.NOT_FOUND;
        if (ex instanceof InsufficientCreditException) return HttpStatus.CONFLICT;
        if (ex instanceof InvalidLoanParameterException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof LoanAlreadyPaidException) return HttpStatus.CONFLICT;
        if (ex instanceof PaymentException) return HttpStatus.BAD_REQUEST;
        return HttpStatus.INTERNAL_SERVER_ERROR; // Default fallback
    }

}
