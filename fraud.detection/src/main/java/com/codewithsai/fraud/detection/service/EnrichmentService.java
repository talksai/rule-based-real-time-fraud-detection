package com.codewithsai.fraud.detection.service;


import com.codewithsai.fraud.detection.model.UserEvent;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EnrichmentService {

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public EnrichmentService(
            @Qualifier("enrichmentCircuitBreaker") CircuitBreaker circuitBreaker,
            @Qualifier("enrichmentRetry") Retry retry) {
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    public Mono<UserEvent> enrich(UserEvent event) {
        // Placeholder for geoip/device enrichment
        return Mono.just(event)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }
}

