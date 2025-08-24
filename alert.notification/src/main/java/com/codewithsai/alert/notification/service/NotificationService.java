package com.codewithsai.alert.notification.service;


import com.codewithsai.alert.notification.model.NotificationRequest;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.reactor.retry.RetryOperator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class NotificationService {

    private final Retry retry;

    public NotificationService(RetryRegistry retryRegistry) {
        this.retry = retryRegistry.retry("notificationRetry");
    }

    public Mono<Void> send(NotificationRequest req) {
        return Mono.fromRunnable(() -> {
                    switch (req.channel().toUpperCase()) {
                        case "EMAIL":
                            sendEmail(req.userId(), req.payload());
                            break;
                        case "SMS":
                            sendSms(req.userId(), req.payload());
                            break;
                        case "SSE":
                            sendSse(req.userId(), req.payload());
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported notification channel: " + req.channel());
                    }
                })
                .transformDeferred(RetryOperator.of(retry))
                .then();
    }

    // Placeholder implementation - replace with actual email provider integration
    private void sendEmail(String userId, String payload) {
        // TODO: Integrate with actual email service provider (e.g., SendGrid, SES)
        System.out.println("Sending EMAIL to user " + userId + ": " + payload);
    }

    // Placeholder implementation - replace with actual SMS provider integration
    private void sendSms(String userId, String payload) {
        // TODO: Integrate with actual SMS gateway (e.g., Twilio)
        System.out.println("Sending SMS to user " + userId + ": " + payload);
    }

    // Placeholder implementation - replace with actual SSE/WebSocket broadcasting
    private void sendSse(String userId, String payload) {
        // TODO: Broadcast to connected clients via SSE or WebSocket
        System.out.println("Broadcasting SSE to user " + userId + ": " + payload);
    }
}
