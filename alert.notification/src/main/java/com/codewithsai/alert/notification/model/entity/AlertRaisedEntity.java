package com.codewithsai.alert.notification.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("alerts")
@Data
public class AlertRaisedEntity {
    @Id
    private String alertId;
    private String userId;
    private String eventId;
    private String ruleId;
    private String severity;
    private String message;
    private Instant timestamp;
    private Map<String, Object> details;
    private String ruleName;
    private String correlationId;
}

