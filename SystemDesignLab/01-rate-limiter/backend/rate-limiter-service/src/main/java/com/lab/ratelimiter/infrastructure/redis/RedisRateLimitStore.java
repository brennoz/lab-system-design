package com.lab.ratelimiter.infrastructure.redis;

import com.lab.ratelimiter.domain.model.RateLimitKey;
import com.lab.ratelimiter.domain.port.RateLimitStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

/**
 * Pattern: Adapter (Hexagonal) — translates the domain's RateLimitStore port to Redis commands.
 * OOP principle: Dependency Inversion — domain depends on the RateLimitStore interface;
 *   this class lives in infrastructure and depends on Spring Redis, never the other way around.
 *
 * Why Lua script for incrementAndGet?
 *   INCR and EXPIRE are two separate Redis commands. Between them, another request on a
 *   replica or concurrent thread could INCR before EXPIRE runs — the key would never expire.
 *   Lua scripts execute atomically in Redis's single-threaded event loop: no interleaving possible.
 *
 * Data structure: Redis String (integer counter).
 *   Why String and not Hash or List?
 *   INCR only works on String keys. Single-field counters need no structure overhead.
 *   GET/INCR on a String key is O(1) — the fastest Redis operation.
 *
 * Why StringRedisTemplate over RedisTemplate<String, Long>?
 *   Redis stores everything as bytes. StringRedisTemplate uses UTF-8 String serialization,
 *   which matches what INCR/GET expect. A typed template would add a serialization layer
 *   that breaks native Redis integer semantics (INCR needs the value to look like an integer string).
 */
public class RedisRateLimitStore implements RateLimitStore {

    // Algorithm: atomic INCR + conditional EXPIRE.
    // KEYS[1] = the counter key, ARGV[1] = TTL in seconds.
    // Condition: only set EXPIRE on the first INCR (count == 1) so subsequent calls
    // don't reset the TTL and accidentally extend the window.
    private static final RedisScript<Long> INCR_WITH_TTL = RedisScript.of("""
            local count = redis.call('INCR', KEYS[1])
            if count == 1 then
                redis.call('EXPIRE', KEYS[1], ARGV[1])
            end
            return count
            """, Long.class);

    private final StringRedisTemplate redis;

    public RedisRateLimitStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public long incrementAndGet(RateLimitKey key, Duration window) {
        Long count = redis.execute(INCR_WITH_TTL,
                List.of(key.value()),
                String.valueOf(window.toSeconds()));
        return count != null ? count : 1L;
    }

    @Override
    public long get(RateLimitKey key) {
        String value = redis.opsForValue().get(key.value());
        // Why 0L for null? A missing key means no requests in that window — count is zero.
        return value != null ? Long.parseLong(value) : 0L;
    }
}
