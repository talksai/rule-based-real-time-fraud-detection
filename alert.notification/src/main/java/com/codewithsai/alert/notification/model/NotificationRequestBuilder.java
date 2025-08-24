package com.codewithsai.alert.notification.model;

import com.codewithsai.alert.notification.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

public class NotificationRequestBuilder {


    /**
     * Builds a NotificationRequest from AlertRaised, including determining channel and payload.
     */
    public static NotificationRequest buildNotificationRequest(AlertRaised alert) {
        String userId = alert.userId();
        String alertId = alert.alertId();
        String channel = determineChannel(alert);
        String payload = formatPayload(alert);

        return new NotificationRequest(userId, alertId, channel, payload);
    }

    private static String determineChannel(AlertRaised alert) {
        return switch (alert.severity().toUpperCase()) {
            case "CRITICAL", "HIGH" -> "EMAIL";
            default -> "SSE";
        };
    }

    private static String formatPayload(AlertRaised alert) {
        Map<String, Object> message = new HashMap<>();
        message.put("alertId", alert.alertId());
        message.put("message", alert.message());
        message.put("severity", alert.severity());
        message.put("timestamp", alert.timestamp().toString());
        message.put("details", alert.details());

        try {
            return JsonUtil.toJsonString(message);
        } catch (Exception e) {
            // Fallback to simple string if serialization fails
            return String.format("[%s] %s", alert.severity(), alert.message());
        }
    }
}
