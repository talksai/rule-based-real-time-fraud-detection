package com.codewithsai.fraud.detection.service;

import com.codewithsai.fraud.detection.model.UserEvent;
import com.codewithsai.fraud.detection.producer.AlertProducer;
//import com.codewithsai.fraud.detection.repository.IdempotencyRepository;
import com.codewithsai.fraud.detection.util.CorrelationIdUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Service
public class FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    private final ObjectMapper mapper;
//    private final IdempotencyRepository idempotencyRepo;
    private final EnrichmentService enrichmentService;
    private final RuleEngineService ruleEngine;
    private final AlertProducer alertProducer;

    public FraudDetectionService(
            ObjectMapper mapper,
//            IdempotencyRepository idempotencyRepo,
            EnrichmentService enrichmentService,
            RuleEngineService ruleEngine,
            AlertProducer alertProducer) {
        this.mapper = mapper;
//        this.idempotencyRepo = idempotencyRepo;
        this.enrichmentService = enrichmentService;
        this.ruleEngine = ruleEngine;
        this.alertProducer = alertProducer;
    }

    @KafkaListener(topics = "${app.kafka.topics.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String json = record.value();
        String correlationId = CorrelationIdUtil.generate(record);

        try {
            UserEvent event = mapper.readValue(json, UserEvent.class);
            log.info("Consumed event id={} [correlationId={}]", event.eventId(), correlationId);

            enrichmentService.enrich(event)
                    .flatMap(enriched -> ruleEngine.evaluateAll(enriched, correlationId))
                    .flatMap(alert -> alertProducer.sendAlert(alert)
                            .contextWrite(ctx -> CorrelationIdUtil.withCorrelationId(ctx, correlationId))
                    )
                    .contextWrite(Context.of(CorrelationIdUtil.CORRELATION_ID_KEY, correlationId))
                    .doOnSuccess(v -> ack.acknowledge())
                    .doOnError(err -> {
                        log.error("Processing error [correlationId={}]", correlationId, err);
                        ack.acknowledge(); // Acknowledge to avoid reprocessing
                    })
                    .subscribe();

        } catch (Exception e) {
            log.error("Deserialization failed: {}", json, e);
            ack.acknowledge();
        }
    }
}
