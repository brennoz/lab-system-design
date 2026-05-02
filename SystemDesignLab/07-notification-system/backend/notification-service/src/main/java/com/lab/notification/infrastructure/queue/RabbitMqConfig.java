package com.lab.notification.infrastructure.queue;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Pattern: Two-lane work queue — CRITICAL and BULK are isolated; each has its own DLQ
@Configuration
public class RabbitMqConfig {

    @Bean
    Queue criticalQueue() {
        return QueueBuilder.durable(RabbitMqTaskQueue.CRITICAL_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RabbitMqTaskQueue.CRITICAL_QUEUE + ".dlq")
                .build();
    }

    @Bean
    Queue criticalDlq() {
        return QueueBuilder.durable(RabbitMqTaskQueue.CRITICAL_QUEUE + ".dlq").build();
    }

    @Bean
    Queue bulkQueue() {
        return QueueBuilder.durable(RabbitMqTaskQueue.BULK_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RabbitMqTaskQueue.BULK_QUEUE + ".dlq")
                .build();
    }

    @Bean
    Queue bulkDlq() {
        return QueueBuilder.durable(RabbitMqTaskQueue.BULK_QUEUE + ".dlq").build();
    }
}
