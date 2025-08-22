package com.codewithsai.fraud.detection.repository;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.reactor.retry.RetryOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class BlacklistRepository {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public BlacklistRepository(
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier("redisCircuitBreaker") CircuitBreaker circuitBreaker,
            @Qualifier("redisRetry") Retry retry) {
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    public Mono<Boolean> isIpBlacklisted(String ip) {
        return redisTemplate.opsForSet()
                .isMember("blacklist:ip", ip)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .onErrorReturn(false);
    }

    public Mono<Boolean> isDeviceBlacklisted(String deviceId) {
        return redisTemplate.opsForSet()
                .isMember("blacklist:device", deviceId)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .onErrorReturn(false);
    }
}

