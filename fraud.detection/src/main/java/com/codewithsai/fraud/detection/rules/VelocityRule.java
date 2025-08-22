package com.codewithsai.fraud.detection.rules;

import com.codewithsai.fraud.detection.model.AlertRaised;
import com.codewithsai.fraud.detection.model.UserEvent;
import com.codewithsai.fraud.detection.model.WindowStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class VelocityRule implements Rule {

    private final int maxEventsPerMinute;
    private final double maxAmountPerMinute;

    public VelocityRule(
            @Value("${app.fraud-rules.velocity.max-events-per-minute:100}") int maxEventsPerMinute,
            @Value("${app.fraud-rules.velocity.max-amount-per-minute:10000.0}") double maxAmountPerMinute) {
        this.maxEventsPerMinute = maxEventsPerMinute;
        this.maxAmountPerMinute = maxAmountPerMinute;
    }

    @Override
    public String getRuleId() {
        return "VELOCITY_RULE";
    }

    @Override
    public String getRuleName() {
        return "Event Velocity Threshold";
    }

    @Override
    public Mono<AlertRaised> evaluate(UserEvent event, WindowStats stats, String correlationId) {
        // Check event count threshold
        if (stats.eventCount() > maxEventsPerMinute) {
            return Mono.just(createAlert(
                    event,
                    "HIGH",
                    "Event velocity exceeded: " + stats.eventCount() + " events in window",
                    Map.of(
                            "eventCount", stats.eventCount(),
                            "threshold", maxEventsPerMinute,
                            "windowStart", stats.windowStart(),
                            "windowEnd", stats.windowEnd()
                    ),
                    correlationId
            ));
        }

        // Check amount threshold
        if (stats.totalAmount() > maxAmountPerMinute) {
            return Mono.just(createAlert(
                    event,
                    "HIGH",
                    "Amount velocity exceeded: $" + stats.totalAmount() + " in window",
                    Map.of(
                            "totalAmount", stats.totalAmount(),
                            "threshold", maxAmountPerMinute,
                            "windowStart", stats.windowStart(),
                            "windowEnd", stats.windowEnd()
                    ),
                    correlationId
            ));
        }

        return Mono.empty();
    }

    @Override
    public boolean isApplicable(UserEvent event) {
        return true; // Always applicable
    }

    private AlertRaised createAlert(UserEvent event, String severity, String message,
                                    Map<String, Object> details, String correlationId) {
        return new AlertRaised(
                UUID.randomUUID().toString(),
                event.userId(),
                event.eventId(),
                getRuleId(),
                getRuleName(),
                severity,
                message,
                Instant.now(),
                details,
                correlationId
        );
    }
}

