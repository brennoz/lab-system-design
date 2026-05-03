package com.lab.flight.infrastructure.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

// Pattern: Dead Letter Topic — poison messages route to .DLT after 3 retries; consumer group never stalls
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafka) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafka,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
        // 3 retries × 1s backoff before routing to DLT
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3));
    }
}
