package com.codewithsai.api.gateway.ratelimit;

import reactor.core.publisher.Mono;

public interface RateLimiter {
    Mono<Boolean> isAllowed(String key);
    Mono<Long> getRemainingTokens(String key);
}

