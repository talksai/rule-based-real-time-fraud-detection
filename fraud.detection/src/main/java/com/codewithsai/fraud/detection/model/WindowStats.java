package com.codewithsai.fraud.detection.model;

import java.time.Instant;

public record WindowStats(
        String key,
        long eventCount,
        double totalAmount,
        Instant windowStart,
        Instant windowEnd
) {}

