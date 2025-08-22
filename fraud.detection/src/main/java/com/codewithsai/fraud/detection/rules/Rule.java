package com.codewithsai.fraud.detection.rules;

import com.codewithsai.fraud.detection.model.AlertRaised;
import com.codewithsai.fraud.detection.model.UserEvent;
import com.codewithsai.fraud.detection.model.WindowStats;
import reactor.core.publisher.Mono;

public interface Rule {
    String getRuleId();
    String getRuleName();
    Mono<AlertRaised> evaluate(UserEvent event, WindowStats stats, String correlationId);
    boolean isApplicable(UserEvent event);
}

