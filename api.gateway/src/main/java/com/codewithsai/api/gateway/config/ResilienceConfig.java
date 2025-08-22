package com.codewithsai.api.gateway.config;

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
import org.springframework.context.annotation.Primary;

@Configuration
public class ResilienceConfig {

    @Bean("redisRateLimiterCircuitBreaker")
    public CircuitBreaker redisRateLimiterCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("redisRateLimiter");
    }

    @Bean("kafkaProducerCircuitBreaker")
    public CircuitBreaker kafkaProducerCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("kafkaProducer");
    }

    @Bean("redisRetry")
    public Retry redisRetry(RetryRegistry registry) {
        return registry.retry("redisRetry");
    }

    @Bean("kafkaRetry")
    public Retry kafkaRetry(RetryRegistry registry) {
        return registry.retry("kafkaRetry");
    }

    @Bean("kafkaBulkhead")
    public Bulkhead kafkaBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("kafkaBulkhead");
    }

    @Bean("redisTimeLimiter")
    public TimeLimiter redisTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("redisTimeout");
    }

    @Bean("kafkaTimeLimiter")
    public TimeLimiter kafkaTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("kafkaTimeout");
    }
}
