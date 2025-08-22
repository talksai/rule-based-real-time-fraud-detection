package com.codewithsai.api.gateway.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.enable-idempotence}")
    private boolean enableIdempotence;

    @Value("${spring.kafka.producer.max-in-flight-requests-per-connection}")
    private int maxInFlight;

    @Value("${spring.kafka.producer.compression-type}")
    private String compressionType;

    @Value("${spring.kafka.producer.batch-size}")
    private int batchSize;

    @Value("${spring.kafka.producer.linger-ms}")
    private int lingerMs;

    @Value("${spring.kafka.producer.buffer-memory}")
    private long bufferMemory;

    @Value("${spring.kafka.producer.request-timeout-ms}")
    private int requestTimeout;

    @Value("${spring.kafka.producer.delivery-timeout-ms}")
    private int deliveryTimeout;

    @Value("${spring.kafka.producer.metrics-sample-window-ms:30000}")
    private long metricsSampleWindow;

    @Value("${spring.kafka.producer.metrics-num-samples:2}")
    private int metricsNumSamples;

    @Bean
    public KafkaSender<String, String> kafkaSender() {
        Map<String, Object> producerProps = new HashMap<>();

        // Basic Configuration
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Reliability Configuration
        producerProps.put(ProducerConfig.ACKS_CONFIG, acks);
        producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        producerProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, maxInFlight);

        // Performance Configuration
        producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        producerProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        producerProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        producerProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);

        // Timeout Configuration
        producerProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        producerProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);

        // Monitoring Configuration
        producerProps.put(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, metricsSampleWindow);
        producerProps.put(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, metricsNumSamples);

        SenderOptions<String, String> senderOptions = SenderOptions.create(producerProps);
        return KafkaSender.create(senderOptions);
    }
}

