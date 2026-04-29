package com.lab.ratelimiter.domain.service;

import com.lab.ratelimiter.domain.model.RateLimit;
import com.lab.ratelimiter.domain.model.RateLimitKey;
import com.lab.ratelimiter.domain.model.RateLimitResult;
import com.lab.ratelimiter.domain.port.RateLimitStore;

import java.time.Duration;
import java.time.Instant;

/**
 * Pattern: Strategy (concrete) — Token Bucket algorithm implementation.
 * Pattern: Domain Service — stateless, logic operates on multiple objects (key + limit + store).
 * OOP principle: Single Responsibility — this class does one thing: apply Token Bucket logic.
 *
 * Algorithm: Token Bucket
 *   - Each window starts with maxRequests tokens.
 *   - Each request consumes 1 token (INCR counter).
 *   - If counter exceeds maxRequests → deny.
 *   - Window resets automatically via Redis TTL.
 *   - Complexity: O(1) per request — one Redis INCR (atomic).
 *
 * Why O(1)? INCR is a single Redis command with no iteration.
 *   Redis processes it in a single-threaded event loop — guaranteed atomic.
 *
 * Data structure (Redis): String key → integer counter.
 *   Why String and not Hash? Counter is a single value — String with INCR is idiomatic Redis.
 *   Hash would add unnecessary field overhead.
 *
 * Why inject RateLimitStore via constructor (not Spring @Autowired on field)?
 *   Constructor injection makes the dependency explicit and mandatory.
 *   Field injection hides dependencies and makes the class hard to instantiate in unit tests.
 *   OOP principle: Explicit Dependencies.
 *
 * STUB — returns allowed=true always. Tests will fail (red phase).
 */
public class TokenBucketRateLimiter implements RateLimiterAlgorithmService {

    private final RateLimitStore store;

    public TokenBucketRateLimiter(RateLimitStore store) {
        this.store = store;
    }

    @Override
    public RateLimitResult check(RateLimitKey key, RateLimit limit) {
        // Algorithm: Token Bucket — O(1): one atomic Redis INCR.
        // The store sets TTL = window on the first INCR; the key expires automatically.
        // No cron job or explicit cleanup needed — Redis TTL is the bucket refill mechanism.
        long count = store.incrementAndGet(key, limit.window());

        // Remaining seconds in the current window — used for both resetAt and retryAfter.
        // Why not just use limit.window()? That would report the full window even if we're halfway through.
        long windowSeconds = limit.window().toSeconds();
        long secondsUntilReset = windowSeconds - (Instant.now().getEpochSecond() % windowSeconds);
        Instant resetAt = Instant.now().plusSeconds(secondsUntilReset);

        if (count > limit.maxRequests()) {
            // RFC 6585: 429 Too Many Requests — Retry-After tells the client exactly when to retry.
            return RateLimitResult.denied(resetAt, Duration.ofSeconds(secondsUntilReset));
        }

        int remaining = (int) Math.max(0, limit.maxRequests() - count);
        return RateLimitResult.allowed(remaining, resetAt);
    }
}
