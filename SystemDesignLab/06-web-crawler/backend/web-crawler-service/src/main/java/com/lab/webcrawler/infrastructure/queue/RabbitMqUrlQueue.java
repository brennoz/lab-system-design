package com.lab.webcrawler.infrastructure.queue;

import com.lab.webcrawler.domain.model.CrawlUrl;
import com.lab.webcrawler.domain.port.UrlQueuePort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

// Pattern: Work queue adapter — publishes CrawlUrl to RabbitMQ url.frontier; no replay needed
public class RabbitMqUrlQueue implements UrlQueuePort {

    static final String QUEUE = "url.frontier";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqUrlQueue(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enqueue(CrawlUrl crawlUrl) {
        rabbitTemplate.convertAndSend(QUEUE, crawlUrl.url() + "|" + crawlUrl.depth());
    }
}
