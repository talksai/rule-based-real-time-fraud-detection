package com.codewithsai.api.gateway.service;

import com.codewithsai.api.gateway.exception.EventValidationException;
import com.codewithsai.api.gateway.model.UserEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class ValidationService {

    private final int maxTimestampFutureSeconds;

    public ValidationService(@Value("${app.validation.max-timestamp-future-seconds:10}") int maxTimestampFutureSeconds) {
        this.maxTimestampFutureSeconds = maxTimestampFutureSeconds;
    }

    public Mono<Void> validateEvent(UserEvent event) {
        return Mono.fromRunnable(() -> {
            if (event.timestamp().isAfter(Instant.now().plusSeconds(maxTimestampFutureSeconds))) {
                throw new EventValidationException("Timestamp cannot be more than " + maxTimestampFutureSeconds + " seconds in the future");
            }

            if (event.timestamp().isBefore(Instant.now().minusSeconds(86400))) { // 24 hours ago
                throw new EventValidationException("Timestamp cannot be older than 24 hours");
            }
        });
    }
}

