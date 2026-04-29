package com.lab.ratelimiter.fixtures;

import com.lab.ratelimiter.domain.model.RateLimit;
import com.lab.ratelimiter.domain.model.RateLimitKey;
import com.lab.ratelimiter.domain.model.RateLimiterAlgorithm;

import java.time.Duration;

/**
 * Pattern: Object Mother — centralised factory for test objects.
 * OOP principle: DRY (Don't Repeat Yourself) — test data built once, reused everywhere.
 *
 * Why static methods on a final class?
 *   No state needed — these are pure factories.
 *   Final prevents accidental subclassing that could introduce state.
 *   Static means callers need no instance: RateLimitFixture.userKey() reads naturally.
 *
 * Why not use a Builder here?
 *   Builders shine when objects have many optional fields.
 *   Our domain objects are small records — named factory methods are more readable.
 *   "RateLimitFixture.strictLimit()" communicates intent; a builder does not.
 */
public final class RateLimitFixture {

    private RateLimitFixture() {} // Utility class — prevent instantiation

    // ── Keys ──────────────────────────────────────────────────────────────

    public static RateLimitKey userKey() {
        return RateLimitKey.of("user", "123");
    }

    public static RateLimitKey ipKey() {
        return RateLimitKey.of("ip", "192.168.1.1");
    }

    public static RateLimitKey apiKey() {
        return RateLimitKey.of("apikey", "abc-def-ghi");
    }

    // ── Rate Limits ───────────────────────────────────────────────────────

    /** 5 requests per 10 seconds — tight limit, easy to exhaust in tests */
    public static RateLimit tokenBucketLimit() {
        return new RateLimit(5, Duration.ofSeconds(10), RateLimiterAlgorithm.TOKEN_BUCKET);
    }

    /** 5 requests per 10 seconds — same numbers, different algorithm */
    public static RateLimit slidingWindowLimit() {
        return new RateLimit(5, Duration.ofSeconds(10), RateLimiterAlgorithm.SLIDING_WINDOW);
    }

    /** Single request allowed — useful for "deny on second request" tests */
    public static RateLimit singleRequestLimit() {
        return new RateLimit(1, Duration.ofSeconds(60), RateLimiterAlgorithm.TOKEN_BUCKET);
    }
}
