package com.lab.urlshortener.infrastructure.bloomfilter;

import com.lab.urlshortener.domain.port.BloomFilterPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// Pattern: Adapter (Bloom Filter) — implements BloomFilterPort using Redis SETBIT/GETBIT
// Data Structure: Bloom Filter — probabilistic bit array; O(k) per add/check where k = HASH_COUNT
// Why Redis bits: persistent across restarts, shared across service instances, ~1.2MB for 10M slots
@Component
public class RedisBloomFilterAdapter implements BloomFilterPort {

    private static final String BLOOM_KEY = "urlshortener:bloom";
    // Why 10_000_000: supports ~700K URLs at 1% false-positive rate (m = -n*ln(p) / ln(2)^2)
    private static final long BIT_SIZE = 10_000_000L;
    // Why 3 hash functions: optimal for this bit size and expected load
    private static final int HASH_COUNT = 3;

    private final StringRedisTemplate redis;

    public RedisBloomFilterAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void add(String value) {
        for (int i = 0; i < HASH_COUNT; i++) {
            redis.opsForValue().setBit(BLOOM_KEY, bitPosition(value, i), true);
        }
    }

    @Override
    public boolean mightContain(String value) {
        for (int i = 0; i < HASH_COUNT; i++) {
            Boolean bit = redis.opsForValue().getBit(BLOOM_KEY, bitPosition(value, i));
            if (!Boolean.TRUE.equals(bit)) return false;
        }
        return true;
    }

    // Why XOR with seed prime: cheap way to produce k independent hash positions from one string
    private long bitPosition(String value, int seed) {
        long hash = value.hashCode() ^ (long) seed * 0x9e3779b9L;
        return Math.abs(hash) % BIT_SIZE;
    }
}
