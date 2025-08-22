package com.codewithsai.api.gateway.exception;

import com.codewithsai.api.gateway.model.ErrorResponse;
import com.codewithsai.api.gateway.util.CorrelationIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);
    private final CorrelationIdGenerator correlationIdGenerator;

    public GlobalErrorHandler(CorrelationIdGenerator correlationIdGenerator) {
        this.correlationIdGenerator = correlationIdGenerator;
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        String correlationId = correlationIdGenerator.generate();
        Map<String, String> fieldErrors = new HashMap<>();

        ex.getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation error: correlationId={}, errors={}", correlationId, fieldErrors);

        ErrorResponse errorResponse = ErrorResponse.of(
                "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors,
                correlationId,
                HttpStatus.BAD_REQUEST.value()
        );

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRateLimitException(RateLimitExceededException ex) {
        String correlationId = correlationIdGenerator.generate();

        log.warn("Rate limit exceeded: correlationId={}, message={}", correlationId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                "RATE_LIMIT_EXCEEDED",
                ex.getMessage(),
                correlationId,
                HttpStatus.TOO_MANY_REQUESTS.value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse));
    }

    @ExceptionHandler(EventValidationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleEventValidationException(EventValidationException ex) {
        String correlationId = correlationIdGenerator.generate();

        log.warn("Event validation error: correlationId={}, message={}", correlationId, ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                "EVENT_VALIDATION_ERROR",
                ex.getMessage(),
                correlationId,
                HttpStatus.BAD_REQUEST.value()
        );

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleParseException(ServerWebInputException ex) {
        String correlationId = correlationIdGenerator.generate();

        log.warn("Request parsing error: correlationId={}", correlationId, ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                "INVALID_REQUEST_FORMAT",
                "Malformed request body",
                correlationId,
                HttpStatus.BAD_REQUEST.value()
        );

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        String correlationId = correlationIdGenerator.generate();

        log.error("Unexpected error: correlationId={}", correlationId, ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                correlationId,
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}
