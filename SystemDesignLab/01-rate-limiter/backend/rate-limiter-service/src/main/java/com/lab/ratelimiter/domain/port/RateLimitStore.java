package com.lab.ratelimiter.domain.port;

import com.lab.ratelimiter.domain.model.RateLimitKey;
import com.lab.ratelimiter.domain.model.RateLimit;

import java.time.Duration;

/**
 * Pattern: Port (Hexagonal Architecture) — the domain defines WHAT storage it needs;
 *   infrastructure decides HOW to provide it.
 * OOP principle: Dependency Inversion — high-level domain depends on this abstraction,
 *   not on Redis directly. RedisRateLimitStore in infrastructure implements this.
 *
 * Why an interface in the domain layer?
 *   The domain service (TokenBucketRateLimiter) calls this port.
 *   In unit tests, a mock implements it — no Redis needed.
 *   In production, RedisRateLimitStore implements it — no domain changes.
 *   Swapping Redis for Memcached means replacing one class, touching zero domain code.
 *
 * Data structure exposed: long (counter value).
 *   Why not return the full RateLimitResult here?
 *   The store is a thin counter abstraction. The DECISION (allowed/denied) belongs
 *   to the domain service, not to the store. Single Responsibility Principle.
 */
public interface RateLimitStore {

    /**
     * Atomically increment the counter for this key and return the new value.
     * Sets TTL on first increment (key expiry = window duration).
     *
     * Why atomic increment?
     *   Two concurrent requests reading then writing is a race condition.
     *   Redis INCR is atomic — one operation, no interleaving.
     *
     * @return new counter value after increment (1 on first request in window)
     */
    long incrementAndGet(RateLimitKey key, Duration window);

    /**
     * Returns the current counter value without modifying it.
     * Used by Sliding Window to read the previous window's count.
     *
     * @return current count, or 0 if the key has expired or never existed
     */
    long get(RateLimitKey key);
}
