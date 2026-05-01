package com.lab.webcrawler.domain.port;

import com.lab.webcrawler.domain.model.CrawlUrl;

// Pattern: Port (Work Queue) — domain enqueues CrawlUrl; RabbitMQ adapter handles AMQP protocol
public interface UrlQueuePort {

    void enqueue(CrawlUrl crawlUrl);
}
