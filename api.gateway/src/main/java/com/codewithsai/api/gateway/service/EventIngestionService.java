package com.codewithsai.api.gateway.service;
import com.codewithsai.api.gateway.exception.RateLimitExceededException;
import com.codewithsai.api.gateway.model.UserEvent;
import com.codewithsai.api.gateway.ratelimit.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionService.class);

    private final RateLimiter rateLimiter;
    private final KafkaProducerService kafkaProducerService;
    private final ValidationService validationService;

    public EventIngestionService(
            RateLimiter rateLimiter,
            KafkaProducerService kafkaProducerService,
            ValidationService validationService) {
        this.rateLimiter = rateLimiter;
        this.kafkaProducerService = kafkaProducerService;
        this.validationService = validationService;
    }

    public Mono<Void> processEvent(UserEvent event, String correlationId) {
        return Mono.fromRunnable(() -> MDC.put("correlationId", correlationId))
                .then(validationService.validateEvent(event))
                .then(rateLimiter.isAllowed(event.userId()))
                .flatMap(allowed -> {
                    if (!allowed) {
                        log.warn("Rate limit exceeded for user: {}, correlationId: {}", event.userId(), correlationId);
                        return Mono.error(new RateLimitExceededException("Rate limit exceeded for user: " + event.userId()));
                    }
                    return kafkaProducerService.sendEvent(event, correlationId);
                })
                .doOnSuccess(unused -> log.info("Event processed successfully: eventId={}, userId={}, correlationId={}",
                        event.eventId(), event.userId(), correlationId))
                .doOnError(error -> log.error("Failed to process event: eventId={}, userId={}, correlationId={}",
                        event.eventId(), event.userId(), correlationId, error))
                .doFinally(signalType -> MDC.clear());
    }
}

