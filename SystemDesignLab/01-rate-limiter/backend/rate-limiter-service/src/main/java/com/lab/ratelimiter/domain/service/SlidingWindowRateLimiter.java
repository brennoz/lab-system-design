package com.lab.ratelimiter.domain.service;

import com.lab.ratelimiter.domain.model.RateLimit;
import com.lab.ratelimiter.domain.model.RateLimitKey;
import com.lab.ratelimiter.domain.model.RateLimitResult;
import com.lab.ratelimiter.domain.port.RateLimitStore;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Pattern: Strategy (concrete) — Sliding Window Counter algorithm implementation.
 * Pattern: Domain Service — stateless, no mutable state; clock is injected.
 * OOP principle: Single Responsibility — only sliding window logic here.
 * OOP principle: Dependency Inversion — depends on Clock interface, not System.currentTimeMillis().
 *
 * Algorithm: Sliding Window Counter
 *   Uses two fixed windows (current + previous) and interpolates between them.
 *
 *   Formula: effectiveCount = currentCount + prevCount × (1 - elapsed / windowSize)
 *
 *   Why interpolation?
 *     A true sliding window would store a timestamp per request — O(N) memory.
 *     Interpolation approximates it in O(1) with two counters.
 *     Accuracy: within ~0.003% of a true sliding window in practice.
 *
 *   Complexity: O(1) per request — two Redis operations (INCR + GET).
 *
 * Data structure (Redis): two String keys (integer counters), one per window epoch.
 *   Key format: "rl:sw:{rateLimitKey}:{epochBucket}"
 *   epochBucket = now.getEpochSecond() / windowSeconds
 *     → all requests in the same window share the same bucket key
 *     → TTL set to 2× window so previous window key is still readable
 *
 * Why Clock injection instead of Instant.now() inline?
 *   Inline Instant.now() is untestable — you cannot control what time the test sees.
 *   A fixed Clock lets tests set elapsed fraction precisely, making timing assertions
 *   deterministic. OCP: the production path uses Clock.systemUTC(); tests use Clock.fixed().
 *
 * Why not a Redis Sorted Set (like Sliding Window Log)?
 *   Sorted Set = one member per request = O(N) memory per user.
 *   At 1M users × 100 req/min = 100M sorted set members. Impractical.
 *   Two counters = O(1) regardless of traffic volume.
 */
public class SlidingWindowRateLimiter implements RateLimiterAlgorithmService {

    private final RateLimitStore store;
    private final Clock clock;

    public SlidingWindowRateLimiter(RateLimitStore store) {
        this(store, Clock.systemUTC());
    }

    // Overload for tests: inject a fixed Clock to make elapsed-fraction assertions deterministic.
    // Public because tests live in a different package (com.lab.ratelimiter.domain vs .domain.service).
    public SlidingWindowRateLimiter(RateLimitStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    @Override
    public RateLimitResult check(RateLimitKey key, RateLimit limit) {
        // Algorithm: Sliding Window Counter — O(1): one INCR + one GET + one multiply.
        //
        // The store receives the original key; it appends the current epoch bucket internally
        // so that each fixed-window bucket has its own counter (no cross-window collision).
        // TTL = 2× window keeps the previous window's counter alive for the GET below.
        long windowSeconds = limit.window().toSeconds();
        long nowEpoch      = Instant.now(clock).getEpochSecond();
        long currentBucket = nowEpoch / windowSeconds;
        long prevBucket    = currentBucket - 1;

        // Current window: atomic INCR (store appends epoch bucket to key internally).
        // We pass the original key; the test stubs this with eq(key).
        long currentCount = store.incrementAndGet(key, limit.window().multipliedBy(2));

        // Previous window: direct GET — no write, so a separate epoch-keyed object is passed.
        // The store does a plain Redis GET on the key's value string.
        RateLimitKey prevKey = new RateLimitKey("sw:" + key.value() + ":" + prevBucket);
        long prevCount = store.get(prevKey);

        // Interpolation: weight the previous window by how much of it is still "visible".
        // elapsedFraction = how far into the current window we are (0.0 → just started, ~1.0 → about to roll)
        // As elapsed grows, the previous window contributes less → boundary bursts are smoothed.
        double elapsedFraction = (double) (nowEpoch % windowSeconds) / windowSeconds;
        double effectiveCount  = currentCount + prevCount * (1.0 - elapsedFraction);

        long secondsUntilReset = windowSeconds - (nowEpoch % windowSeconds);
        Instant resetAt        = Instant.now(clock).plusSeconds(secondsUntilReset);

        if (effectiveCount > limit.maxRequests()) {
            return RateLimitResult.denied(resetAt, Duration.ofSeconds(secondsUntilReset));
        }

        int remaining = (int) Math.max(0, limit.maxRequests() - (long) effectiveCount);
        return RateLimitResult.allowed(remaining, resetAt);
    }
}
