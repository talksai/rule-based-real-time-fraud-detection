package com.codewithsai.alert.notification.repository;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class RedisAlertCache {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final ReactiveStringRedisTemplate redis;
    private final Bulkhead redisBulkhead;
    private final TimeLimiter redisTimeLimiter;

    public RedisAlertCache(
            ReactiveStringRedisTemplate redis,
            @Qualifier("redisBulkhead") Bulkhead redisBulkhead,
            @Qualifier("redisTimeLimiter") TimeLimiter redisTimeLimiter
    ) {
        this.redis = redis;
        this.redisBulkhead = redisBulkhead;
        this.redisTimeLimiter = redisTimeLimiter;
    }

    public Mono<Boolean> cache(String userId, String payload) {
        String key = "recentAlerts:" + userId;
        return redis.opsForList().leftPush(key, payload)
                .then(redis.opsForList().trim(key, 0, 99))
                .then(redis.expire(key, TTL))
                .transformDeferred(BulkheadOperator.of(redisBulkhead))
                .transformDeferred(TimeLimiterOperator.of(redisTimeLimiter));
    }

}
