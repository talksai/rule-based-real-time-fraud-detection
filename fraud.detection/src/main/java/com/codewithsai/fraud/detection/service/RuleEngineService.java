package com.codewithsai.fraud.detection.service;

import com.codewithsai.fraud.detection.model.AlertRaised;
import com.codewithsai.fraud.detection.model.UserEvent;
import com.codewithsai.fraud.detection.repository.StatsRepository;
import com.codewithsai.fraud.detection.rules.Rule;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RuleEngineService {

    private final List<Rule> rules;
    private final StatsRepository statsRepository;

    public RuleEngineService(List<Rule> rules, StatsRepository statsRepository) {
        this.rules = rules;
        this.statsRepository = statsRepository;
    }

    public Mono<AlertRaised> evaluateAll(UserEvent event, String correlationId) {
        String key = event.getPartitionKey();
        return statsRepository.updateAndGetStats(key, event.amount(), event.timestamp())
                .flatMapMany(stats -> Flux.fromIterable(rules)
                        .filter(rule -> rule.isApplicable(event))
                        .concatMap(rule -> rule.evaluate(event, stats, correlationId))
                )
                .next(); // first alert only
    }
}
