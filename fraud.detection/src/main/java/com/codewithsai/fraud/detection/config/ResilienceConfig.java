package com.codewithsai.fraud.detection.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    @Bean("enrichmentCircuitBreaker")
    public CircuitBreaker enrichmentCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("enrichmentService");
    }

    @Bean("redisCircuitBreaker")
    public CircuitBreaker redisCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("redisOperations");
    }

    @Bean("kafkaCircuitBreaker")
    public CircuitBreaker kafkaCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("kafkaProducer");
    }

    @Bean("enrichmentRetry")
    public Retry enrichmentRetry(RetryRegistry registry) {
        return registry.retry("enrichmentRetry");
    }

    @Bean("redisRetry")
    public Retry redisRetry(RetryRegistry registry) {
        return registry.retry("redisRetry");
    }

    @Bean("kafkaRetry")
    public Retry kafkaRetry(RetryRegistry registry) {
        return registry.retry("kafkaRetry");
    }

    @Bean("enrichmentBulkhead")
    public Bulkhead enrichmentBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("enrichmentBulkhead");
    }

    @Bean("ruleProcessingBulkhead")
    public Bulkhead ruleProcessingBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("ruleProcessingBulkhead");
    }

    @Bean("enrichmentTimeLimiter")
    public TimeLimiter enrichmentTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("enrichmentTimeout");
    }

    @Bean("redisTimeLimiter")
    public TimeLimiter redisTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("redisTimeout");
    }
}
