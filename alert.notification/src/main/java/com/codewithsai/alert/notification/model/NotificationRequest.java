package com.codewithsai.alert.notification.model;

public record NotificationRequest(
        String userId,
        String alertId,
        String channel,   // EMAIL | SMS | SSE
        String payload
) {}
