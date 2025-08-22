package com.codewithsai.api.gateway.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.Instant;

public record UserEvent(
        @NotBlank(message = "Event ID is required")
        @Size(max = 100, message = "Event ID cannot exceed 100 characters")
        String eventId,

        @NotBlank(message = "Event type is required")
        @Size(max = 50, message = "Event type cannot exceed 50 characters")
        String eventType,

        @NotBlank(message = "User ID is required")
        @Size(max = 100, message = "User ID cannot exceed 100 characters")
        String userId,

        @NotNull(message = "Timestamp is required")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant timestamp,

        @Pattern(
                regexp = "^$|^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$|^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
                message = "Invalid IP address format"
        )
        @Size(max = 45, message = "IP address cannot exceed 45 characters")
        String ipAddress,

        @Size(max = 500, message = "User agent cannot exceed 500 characters")
        String userAgent,

        @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
        @DecimalMax(value = "999999999.99", message = "Amount cannot exceed 999999999.99")
        Double amount
) {}
