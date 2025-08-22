package com.codewithsai.fraud.detection.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.nio.charset.StandardCharsets;

public final class CorrelationIdUtil {

    public static final String CORRELATION_ID_KEY = "correlationId";

    private CorrelationIdUtil() {}

    public static String generate(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader("correlationId");
        if (header != null) {
            return new String(header.value(), StandardCharsets.UTF_8);
        }
        return null;
    }

    public static Context withCorrelationId(Context ctx, String correlationId) {
        return ctx.put(CORRELATION_ID_KEY, correlationId);
    }

    public static String get(ContextView ctx) {
        return ctx.getOrDefault(CORRELATION_ID_KEY, "unknown");
    }
}
