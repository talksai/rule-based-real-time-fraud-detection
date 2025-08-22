package com.codewithsai.fraud.detection.repository;

import com.codewithsai.fraud.detection.model.WindowStats;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Repository
public class StatsRepository {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public StatsRepository(
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier("redisCircuitBreaker") CircuitBreaker circuitBreaker,
            @Qualifier("redisRetry") Retry retry) {
        this.redisTemplate = redisTemplate;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
    }

    public Mono<WindowStats> updateAndGetStats(String key, double amount, Instant timestamp) {
        long epochMinute = timestamp.getEpochSecond() / 60;
        String countKey = "stats:count:" + key + ":" + epochMinute;
        String amountKey = "stats:amount:" + key + ":" + epochMinute;

        Mono<Long> countMono = redisTemplate.opsForValue().increment(countKey)
                .flatMap(count -> {
                    if (count == 1L) {
                        return redisTemplate.expire(countKey, Duration.ofMinutes(5))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                });

        Mono<Double> amountMono = amount > 0 ?
                redisTemplate.opsForValue().increment(amountKey, amount)
                        .flatMap(total -> {
                            if (total.equals(amount)) {
                                return redisTemplate.expire(amountKey, Duration.ofMinutes(5))
                                        .thenReturn(total);
                            }
                            return Mono.just(total);
                        }) :
                Mono.just(0.0);

        return Mono.zip(countMono, amountMono)
                .map(tuple -> new WindowStats(
                        key,
                        tuple.getT1(),
                        tuple.getT2(),
                        Instant.ofEpochSecond(epochMinute * 60),
                        Instant.ofEpochSecond((epochMinute + 1) * 60)
                ))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .onErrorReturn(new WindowStats(key, 0, 0.0, timestamp, timestamp));
    }

    public Mono<Void> updateBaseline(String key, double mean, double stdDev) {
        String baselineKey = "baseline:" + key;
        return redisTemplate.opsForHash()
                .putAll(baselineKey, Map.of(
                        "mean", String.valueOf(mean),
                        "stdDev", String.valueOf(stdDev),
                        "timestamp", String.valueOf(Instant.now().getEpochSecond())
                ))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .onErrorResume(throwable -> Mono.empty())
                .then();
    }

    public Mono<double[]> getBaseline(String key) {
        String baselineKey = "baseline:" + key;
        return redisTemplate.opsForHash()
                .multiGet(baselineKey, java.util.List.of("mean", "stdDev"))
                .map(values -> {
                    if (values.get(0) == null || values.get(1) == null) {
                        return new double[]{0.0, 0.0};
                    }
                    double mean = Double.parseDouble(values.get(0).toString());
                    double stdDev = Double.parseDouble(values.get(1).toString());
                    return new double[]{mean, stdDev};
                })
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry))
                .onErrorReturn(new double[]{0.0, 0.0});
    }
}

