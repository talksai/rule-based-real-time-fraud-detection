package com.codewithsai.api.gateway.model;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        boolean success,
        String error,
        String message,
        Map<String, String> details,
        String correlationId,
        Instant timestamp,
        int status
) {
    public static ErrorResponse of(String error, String message, String correlationId, int status) {
        return new ErrorResponse(false, error, message, null, correlationId, Instant.now(), status);
    }

    public static ErrorResponse of(String error, String message, Map<String, String> details, String correlationId, int status) {
        return new ErrorResponse(false, error, message, details, correlationId, Instant.now(), status);
    }
}

