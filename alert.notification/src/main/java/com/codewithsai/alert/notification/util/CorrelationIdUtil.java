package com.codewithsai.alert.notification.util;

import com.codewithsai.alert.notification.model.AlertRaised;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class CorrelationIdUtil {

    public static final String CORRELATION_ID_KEY = "correlationId";

    private CorrelationIdUtil() {}

    public static String generate(ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(CORRELATION_ID_KEY);
        return header != null
                ? new String(header.value(), StandardCharsets.UTF_8)
                : generate();
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }


    public static Context withCorrelationId(Context ctx, String correlationId) {
        return ctx.put(CORRELATION_ID_KEY, correlationId);
    }

    public static String get(ContextView ctx) {
        return ctx.getOrDefault(CORRELATION_ID_KEY, "unknown");
    }
}
