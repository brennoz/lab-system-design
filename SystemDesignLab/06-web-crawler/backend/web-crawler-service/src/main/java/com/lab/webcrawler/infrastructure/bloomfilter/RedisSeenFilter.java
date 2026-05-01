package com.lab.webcrawler.infrastructure.bloomfilter;

import com.lab.webcrawler.domain.port.SeenFilterPort;
import org.springframework.data.redis.core.StringRedisTemplate;

// Pattern: Bloom Filter — SETBIT/GETBIT on a Redis bit array; same algorithm as project 05
// Algorithm: 3 hash functions over 10M bits → ~1% false-positive rate at 100k URLs
public class RedisSeenFilter implements SeenFilterPort {

    private static final String KEY = "crawler:bloom";
    private static final long BIT_SIZE = 10_000_000L;

    private final StringRedisTemplate redis;

    public RedisSeenFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void add(String url) {
        for (long offset : offsets(url)) {
            redis.opsForValue().setBit(KEY, offset, true);
        }
    }

    @Override
    public boolean mightContain(String url) {
        for (long offset : offsets(url)) {
            Boolean bit = redis.opsForValue().getBit(KEY, offset);
            if (bit == null || !bit) return false;
        }
        return true;
    }

    private long[] offsets(String value) {
        int h1 = value.hashCode();
        int h2 = value.hashCode() * 31 + value.length();
        int h3 = h1 ^ (h2 << 16);
        return new long[]{
                Math.abs(h1 % BIT_SIZE),
                Math.abs(h2 % BIT_SIZE),
                Math.abs(h3 % BIT_SIZE)
        };
    }
}
