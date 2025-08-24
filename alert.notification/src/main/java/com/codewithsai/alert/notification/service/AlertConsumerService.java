package com.codewithsai.alert.notification.service;


import com.codewithsai.alert.notification.model.AlertRaised;
import com.codewithsai.alert.notification.model.NotificationRequest;
import com.codewithsai.alert.notification.model.NotificationRequestBuilder;
import com.codewithsai.alert.notification.util.CorrelationIdUtil;
import com.codewithsai.alert.notification.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;


@Service
public class AlertConsumerService {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumerService.class);

    private final AlertPersistenceService persistence;
    private final AlertCacheService cache;
    private final NotificationService notificationService;
    private final io.github.resilience4j.circuitbreaker.CircuitBreaker kafkaCircuitBreaker;
    private final ObjectMapper mapper;

    public AlertConsumerService(
            AlertPersistenceService persistence,
            AlertCacheService cache,
            NotificationService notificationService,
            CircuitBreakerRegistry registry, ObjectMapper mapper
    ) {
        this.persistence = persistence;
        this.cache = cache;
        this.notificationService = notificationService;
        this.kafkaCircuitBreaker = registry.circuitBreaker("kafkaConsumer");
        this.mapper = mapper;
    }

    @KafkaListener(topics = "${app.kafka.topics.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {

        String json = record.value();
        String correlationId = CorrelationIdUtil.generate(record);


        try {
            AlertRaised alert = mapper.readValue(json, AlertRaised.class);
            log.info("Received alert id={} for userId={} [correlationId={}]", alert.alertId(), alert.userId(), correlationId);
            Mono.just(alert)
                    .contextWrite(ctx -> CorrelationIdUtil.withCorrelationId(ctx, correlationId))
                    .transformDeferred(CircuitBreakerOperator.of(kafkaCircuitBreaker))
                    .flatMap(persistence::save)
                    .flatMap(savedAlert -> cache.cache(savedAlert.userId(), toJson(savedAlert))
                            .then(Mono.defer(() -> {
                                NotificationRequest notificationRequest = NotificationRequestBuilder.buildNotificationRequest(savedAlert);
                                return notificationService.send(notificationRequest);
                            }))
                    )
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(100)))
                    .doOnError(error -> log.error("Error processing alert with id={}, correlationId={}", alert.alertId(), correlationId, error))
                    .doFinally(signalType -> ack.acknowledge())
                    .subscribe();
        } catch (Exception e) {
            log.error("Deserialization failed: {}", json, e);
            ack.acknowledge();
        }

    }

    private String toJson(AlertRaised alert) {
        try {
            return mapper.writeValueAsString(alert);
        } catch (Exception e) {
            log.error("Failed to serialize AlertRaised id={}", alert.alertId(), e);
            return "{}";
        }
    }

}
