package com.codewithsai.fraud.detection.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UserEvent(
        @NotBlank String eventId,
        @NotBlank String eventType,
        @NotBlank String userId,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant timestamp,
        String ipAddress,
        String userAgent,
        Double amount,
        String sessionId,
        String deviceId
) {
    public double getAmount() {
        return amount != null ? amount : 0.0;
    }
    public String getPartitionKey() {
        return userId != null ? userId : (ipAddress != null ? ipAddress : "unknown");
    }
}

