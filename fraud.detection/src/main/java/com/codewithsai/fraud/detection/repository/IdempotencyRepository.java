//package com.codewithsai.fraud.detection.repository;
//
//import io.github.resilience4j.circuitbreaker.CircuitBreaker;
//import io.github.resilience4j.retry.Retry;
//import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
//import io.github.resilience4j.reactor.retry.RetryOperator;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Mono;
//
//import java.time.Duration;
//
//@Repository
//public class IdempotencyRepository {
//
//    private static final Logger log = LoggerFactory.getLogger(IdempotencyRepository.class);
//    private final ReactiveStringRedisTemplate redisTemplate;
//    private final CircuitBreaker circuitBreaker;
//    private final Retry retry;
//    private final Duration ttl;
//
//    public IdempotencyRepository(
//            ReactiveStringRedisTemplate redisTemplate,
//            @Qualifier("redisCircuitBreaker") CircuitBreaker circuitBreaker,
//            @Qualifier("redisRetry") Retry retry,
//            @Value("${app.processing.idempotency-ttl-minutes:24}") int ttlMinutes) {
//        this.redisTemplate = redisTemplate;
//        this.circuitBreaker = circuitBreaker;
//        this.retry = retry;
//        this.ttl = Duration.ofMinutes(ttlMinutes);
//    }
//
//    public Mono<Boolean> markProcessed(String eventId) {
//        String key = "processed:" + eventId;
//        return redisTemplate.opsForValue().setIfAbsent(key, "1")
//                .flatMap(first -> first
//                        ? redisTemplate.expire(key, ttl).thenReturn(true)
//                        : Mono.just(false))
//                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
//                .transformDeferred(RetryOperator.of(retry))
//                .onErrorResume(e -> {
//                    log.error("Error marking event as processed: {}" , e.getMessage());
//                    return Mono.just(false);
//                });
//    }
//}
//
