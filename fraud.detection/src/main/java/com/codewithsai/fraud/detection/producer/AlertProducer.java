package com.codewithsai.fraud.detection.producer;

import com.codewithsai.fraud.detection.model.AlertRaised;
import com.codewithsai.fraud.detection.util.CorrelationIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class AlertProducer {

    private static final Logger log = LoggerFactory.getLogger(AlertProducer.class);
    private final KafkaSender<String, String> kafkaSender;
    private final ObjectMapper mapper;
    private final String topic;
    private final CircuitBreaker kafkaCb;
    private final Retry kafkaRetry;

    public AlertProducer(
            KafkaSender<String, String> kafkaSender,
            ObjectMapper mapper,
            @Value("${app.kafka.topics.output}") String topic,
            @Qualifier("kafkaCircuitBreaker") CircuitBreaker kafkaCb,
            @Qualifier("kafkaRetry") Retry kafkaRetry
    ) {
        this.kafkaSender = kafkaSender;
        this.mapper = mapper;
        this.topic = topic;
        this.kafkaCb = kafkaCb;
        this.kafkaRetry = kafkaRetry;
    }

    public Mono<Void> sendAlert(AlertRaised alert) {
        log.info("sending alerts ,{}",alert.alertId());
        return Mono.deferContextual(ctxView -> {
            String correlationId = CorrelationIdUtil.get(ctxView);
            String payload;
            try {
                payload = mapper.writeValueAsString(alert);
            } catch (Exception e) {
                return Mono.error(e);
            }
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, alert.userId(), payload);
            record.headers().add(CorrelationIdUtil.CORRELATION_ID_KEY, correlationId.getBytes(StandardCharsets.UTF_8));
            SenderRecord<String, String, UUID> senderRecord = SenderRecord.create(record, UUID.randomUUID());

            return kafkaSender.send(Mono.just(senderRecord))
                    .transformDeferred(CircuitBreakerOperator.of(kafkaCb))
                    .transformDeferred(RetryOperator.of(kafkaRetry))
                    .then();
        });
    }
}
