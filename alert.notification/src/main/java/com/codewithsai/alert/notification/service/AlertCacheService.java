package com.codewithsai.alert.notification.service;

import com.codewithsai.alert.notification.repository.RedisAlertCache;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AlertCacheService {

    private final RedisAlertCache cache;
    public AlertCacheService(RedisAlertCache cache) { this.cache = cache; }
    public Mono<Boolean> cache(String u, String p) { return cache.cache(u, p); }
}
