package com.codewithsai.alert.notification.model;

import java.time.Instant;
import java.util.Map;

public record AlertRaised(
        String alertId,
        String userId,
        String eventId,
        String ruleId,
        String ruleName,
        String severity,
        String message,
        Instant timestamp,
        Map<String, Object> details,
        String correlationId
) {}

