package com.lab.webcrawler.infrastructure.queue;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Pattern: Work queue — durable queue with DLQ so failed messages are not silently dropped
@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue urlFrontierQueue() {
        return QueueBuilder.durable(RabbitMqUrlQueue.QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", RabbitMqUrlQueue.QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue urlFrontierDlq() {
        return QueueBuilder.durable(RabbitMqUrlQueue.QUEUE + ".dlq").build();
    }
}
