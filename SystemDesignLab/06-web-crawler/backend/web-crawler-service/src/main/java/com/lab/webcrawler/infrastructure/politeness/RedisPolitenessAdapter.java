package com.lab.webcrawler.infrastructure.politeness;

import com.lab.webcrawler.domain.model.Domain;
import com.lab.webcrawler.domain.port.PolitenessPort;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

// Pattern: Politeness lock — Redis SET EX enforces 1 req/sec per domain; key expires automatically
public class RedisPolitenessAdapter implements PolitenessPort {

    private static final String PREFIX = "crawl:lock:";
    private static final Duration TTL = Duration.ofSeconds(1);

    private final StringRedisTemplate redis;

    public RedisPolitenessAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean isLocked(Domain domain) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + domain.value()));
    }

    @Override
    public void lock(Domain domain) {
        redis.opsForValue().set(PREFIX + domain.value(), "", TTL);
    }
}
