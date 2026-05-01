package com.lab.webcrawler.infrastructure.queue;

import com.lab.webcrawler.application.usecase.CrawlUrlUseCase;
import com.lab.webcrawler.domain.model.CrawlUrl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Pattern: Message consumer — decodes pipe-delimited message, delegates to CrawlUrlUseCase
@Component
public class RabbitMqCrawlConsumer {

    private final CrawlUrlUseCase crawlUrlUseCase;

    public RabbitMqCrawlConsumer(CrawlUrlUseCase crawlUrlUseCase) {
        this.crawlUrlUseCase = crawlUrlUseCase;
    }

    @RabbitListener(queues = RabbitMqUrlQueue.QUEUE)
    public void onMessage(String message) {
        int sep = message.lastIndexOf('|');
        String url = message.substring(0, sep);
        int depth = Integer.parseInt(message.substring(sep + 1));
        crawlUrlUseCase.crawl(new CrawlUrl(url, depth));
    }
}
