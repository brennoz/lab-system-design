package com.lab.urlshortener.infrastructure.cache;

import com.lab.urlshortener.domain.port.CachePort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

// Pattern: Adapter (Cache-Aside) — implements CachePort using Redis GET/SET with TTL
// Why StringRedisTemplate over RedisTemplate<Object,Object>: keys and values are strings; avoids Java serialization overhead
@Component
public class RedisCacheAdapter implements CachePort {

    // Why 24h TTL: balances Redis memory against DB hit rate for typical link lifetimes
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redis;

    public RedisCacheAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(redis.opsForValue().get(key));
    }

    @Override
    public void put(String key, String value) {
        redis.opsForValue().set(key, value, TTL);
    }
}
