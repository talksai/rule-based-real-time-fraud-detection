package com.codewithsai.api.gateway.service;

import com.codewithsai.api.gateway.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper objectMapper;
    private final String topicName;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final Bulkhead bulkhead;

    public KafkaProducerService(
            KafkaSender<String, String> kafkaSender,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topics.events-raw}") String topicName,
            @Qualifier("kafkaProducerCircuitBreaker") CircuitBreaker kafkaProducerCircuitBreaker,
            @Qualifier("kafkaRetry") Retry kafkaRetry,
            @Qualifier("kafkaBulkhead") Bulkhead kafkaBulkhead) {
        this.kafkaSender = kafkaSender;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
        this.circuitBreaker = kafkaProducerCircuitBreaker;
        this.retry = kafkaRetry;
        this.bulkhead = kafkaBulkhead;
    }

    public Mono<Void> sendEvent(UserEvent event, String correlationId) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .map(json -> createSenderRecord(event, json, correlationId))
                .flatMap(senderRecord ->
                        kafkaSender.send(Mono.just(senderRecord))
                                .doOnNext(result -> log.debug("Event sent successfully: eventId={}, correlationId={}",
                                        event.eventId(), correlationId))
                                .doOnError(error -> log.error("Failed to send event: eventId={}, correlationId={}",
                                        event.eventId(), correlationId, error))
                                .then()
                )
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }

    private SenderRecord<String, String, UUID> createSenderRecord(UserEvent event, String json, String correlationId) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                topicName,
                event.userId(),
                json
        );

        // Add correlation ID as header
        record.headers().add("correlationId", correlationId.getBytes());
        record.headers().add("eventType", event.eventType().getBytes());

        return SenderRecord.create(record, UUID.randomUUID());
    }
}

