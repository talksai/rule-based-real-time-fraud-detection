package com.codewithsai.api.gateway.ratelimit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RedisRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);
    private static final String KEY_PREFIX = "rl:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final int maxRequestsPerMinute;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public RedisRateLimiter(
            ReactiveStringRedisTemplate redisTemplate,
            @Value("${app.rate-limit.max-requests-per-minute:100}") int maxRequestsPerMinute,
            @Qualifier("redisRateLimiterCircuitBreaker") CircuitBreaker redisRateLimiterCircuitBreaker,
            @Qualifier("redisRetry") Retry redisRetry) {
        this.redisTemplate = redisTemplate;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.circuitBreaker = redisRateLimiterCircuitBreaker;
        this.retry = redisRetry;
    }

    @Override
    public Mono<Boolean> isAllowed(String key) {
        String redisKey = KEY_PREFIX + key;

        return redisTemplate.opsForValue().increment(redisKey)
                .flatMap(count -> {
                    if (count == 1L) {
                        return redisTemplate.expire(redisKey, Duration.ofMinutes(1))
                                .thenReturn(true);
                    }
                    return Mono.just(count <= maxRequestsPerMinute);
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .onErrorResume(throwable -> {
                    log.error("Rate limiter failed for key: {}, allowing request (fail-open)", key, throwable);
                    return Mono.just(true); // Fail-open policy
                })
                .doOnNext(allowed -> log.debug("Rate limit check for key {}: {}", key, allowed ? "ALLOWED" : "DENIED"));
    }

    @Override
    public Mono<Long> getRemainingTokens(String key) {
        String redisKey = KEY_PREFIX + key;

        return redisTemplate.opsForValue().get(redisKey)
                .map(value -> {
                    long current = Long.parseLong(value);
                    return Math.max(0, maxRequestsPerMinute - current);
                })
                .switchIfEmpty(Mono.just((long) maxRequestsPerMinute))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .onErrorReturn(0L);
    }
}
