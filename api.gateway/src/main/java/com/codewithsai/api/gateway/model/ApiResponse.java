package com.codewithsai.api.gateway.model;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        String correlationId,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return new ApiResponse<>(true, data, "Request processed successfully", correlationId, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, String correlationId) {
        return new ApiResponse<>(true, null, message, correlationId, Instant.now());
    }
}

