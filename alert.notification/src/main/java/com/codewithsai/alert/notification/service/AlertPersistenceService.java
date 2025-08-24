package com.codewithsai.alert.notification.service;

import com.codewithsai.alert.notification.model.AlertRaised;
import com.codewithsai.alert.notification.repository.AlertRepository;
import com.codewithsai.alert.notification.util.AlertRaisedMapper;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.timelimiter.TimeLimiter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AlertPersistenceService {

    private final AlertRepository repo;
    private final Bulkhead mongoBulkhead;
    private final TimeLimiter mongoTimeLimiter;
    private final AlertRaisedMapper alertRaisedMapper;

    public AlertPersistenceService(
            AlertRepository repo,
            @Qualifier("mongoBulkhead") Bulkhead mongoBulkhead,
            @Qualifier("mongoTimeLimiter") TimeLimiter mongoTimeLimiter, AlertRaisedMapper alertRaisedMapper
    ) {
        this.repo = repo;
        this.mongoBulkhead = mongoBulkhead;
        this.mongoTimeLimiter = mongoTimeLimiter;
        this.alertRaisedMapper = alertRaisedMapper;
    }

    public Mono<AlertRaised> save(AlertRaised alert) {
        return repo.save(alertRaisedMapper.toEntity(alert))
                .map(alertRaisedMapper::fromEntity)
                .transformDeferred(BulkheadOperator.of(mongoBulkhead))
                .transformDeferred(TimeLimiterOperator.of(mongoTimeLimiter));
    }

}

