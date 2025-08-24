package com.codewithsai.alert.notification.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }

    @Bean("redisBulkhead")
    public Bulkhead redisBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("redisBulkhead");
    }

    @Bean("mongoBulkhead")
    public Bulkhead mongoBulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("mongoBulkhead");
    }

    @Bean("redisTimeLimiter")
    public TimeLimiter redisTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("redisTimeLimiter");
    }

    @Bean("mongoTimeLimiter")
    public TimeLimiter mongoTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("mongoTimeLimiter");
    }
}
