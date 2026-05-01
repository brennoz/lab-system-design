package com.lab.webcrawler.infrastructure.config;

import com.lab.webcrawler.application.usecase.CrawlUrlUseCase;
import com.lab.webcrawler.application.usecase.SubmitSeedUseCase;
import com.lab.webcrawler.domain.service.LinkExtractor;
import com.lab.webcrawler.domain.service.UrlNormaliser;
import com.lab.webcrawler.infrastructure.bloomfilter.RedisSeenFilter;
import com.lab.webcrawler.infrastructure.fetcher.HttpPageFetcher;
import com.lab.webcrawler.infrastructure.persistence.JpaPageRepository;
import com.lab.webcrawler.infrastructure.persistence.SpringDataPageRepository;
import com.lab.webcrawler.infrastructure.politeness.RedisPolitenessAdapter;
import com.lab.webcrawler.infrastructure.queue.RabbitMqUrlQueue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

// Pattern: Composition root — wires all ports to their adapters; only place Spring sees the domain
@Configuration
public class AppConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    JpaPageRepository jpaPageRepository(SpringDataPageRepository delegate) {
        return new JpaPageRepository(delegate);
    }

    @Bean
    RedisSeenFilter redisSeenFilter(StringRedisTemplate redis) {
        return new RedisSeenFilter(redis);
    }

    @Bean
    RabbitMqUrlQueue rabbitMqUrlQueue(RabbitTemplate rabbitTemplate) {
        return new RabbitMqUrlQueue(rabbitTemplate);
    }

    @Bean
    RedisPolitenessAdapter redisPolitenessAdapter(StringRedisTemplate redis) {
        return new RedisPolitenessAdapter(redis);
    }

    @Bean
    HttpPageFetcher httpPageFetcher(RestTemplate restTemplate) {
        return new HttpPageFetcher(restTemplate);
    }

    @Bean
    SubmitSeedUseCase submitSeedUseCase(RedisSeenFilter seenFilter, RabbitMqUrlQueue urlQueue) {
        return new SubmitSeedUseCase(seenFilter, urlQueue);
    }

    @Bean
    CrawlUrlUseCase crawlUrlUseCase(HttpPageFetcher fetcher, RedisSeenFilter seenFilter,
                                    RabbitMqUrlQueue urlQueue, RedisPolitenessAdapter politeness,
                                    JpaPageRepository pageRepository) {
        return new CrawlUrlUseCase(fetcher, seenFilter, urlQueue, politeness, pageRepository,
                new LinkExtractor(), new UrlNormaliser());
    }
}
