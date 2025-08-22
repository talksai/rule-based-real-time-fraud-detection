package com.codewithsai.api.gateway.controller;

import com.codewithsai.api.gateway.model.ApiResponse;
import com.codewithsai.api.gateway.model.UserEvent;
import com.codewithsai.api.gateway.service.EventIngestionService;
import com.codewithsai.api.gateway.util.CorrelationIdGenerator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventIngestionService eventIngestionService;
    private final CorrelationIdGenerator correlationIdGenerator;
    private final String correlationHeaderName;

    public EventController(
            EventIngestionService eventIngestionService,
            CorrelationIdGenerator correlationIdGenerator,
            @Value("${app.correlation.header-name:X-Correlation-ID}") String correlationHeaderName) {
        this.eventIngestionService = eventIngestionService;
        this.correlationIdGenerator = correlationIdGenerator;
        this.correlationHeaderName = correlationHeaderName;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<ApiResponse<Void>>> ingestEvent(
            @Valid @RequestBody UserEvent event,
            ServerWebExchange exchange) {

        String correlationId = correlationIdGenerator.extractOrGenerate(
                exchange.getRequest().getHeaders().getFirst(correlationHeaderName)
        );

        return eventIngestionService.processEvent(event, correlationId)
                .map(unused -> ResponseEntity
                        .status(HttpStatus.ACCEPTED)
                        .header(correlationHeaderName, correlationId)
                        .body(ApiResponse.success("Event accepted for processing", correlationId)));
    }
}
