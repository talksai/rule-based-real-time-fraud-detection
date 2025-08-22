package com.codewithsai.api.gateway.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CorrelationIdGenerator {

    public String generate() {
        return UUID.randomUUID().toString();
    }

    public String extractOrGenerate(String existingId) {
        return existingId != null && !existingId.trim().isEmpty()
                ? existingId.trim()
                : generate();
    }
}

