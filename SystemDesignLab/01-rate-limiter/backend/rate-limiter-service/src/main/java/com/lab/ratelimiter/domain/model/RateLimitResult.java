package com.lab.ratelimiter.domain.model;

import java.time.Duration;
import java.time.Instant;

/**
 * Pattern: Value Object — immutable outcome of a rate limit check.
 * OOP principle: Tell, Don't Ask — callers don't inspect raw counters and decide;
 *   the domain produces a result that already contains the decision.
 *
 * Why include retryAfter even when allowed=true?
 *   When allowed=true, retryAfter is Duration.ZERO (no wait needed).
 *   Keeping the field always present simplifies callers — no null checks.
 *   OOP principle: Uniform Access — all results have the same shape.
 *
 * Why Instant for resetAt instead of long epoch millis?
 *   Instant is self-describing — no ambiguity about whether it's seconds or millis.
 *   Instant.toEpochMilli() converts trivially when needed for HTTP headers.
 *
 * Data structure: record (product type) — a tuple of four related values
 *   that have no meaning individually. They must travel together.
 *   A Map<String,Object> would lose type safety. A record preserves it.
 */
public record RateLimitResult(
        boolean allowed,
        int remaining,
        Instant resetAt,
        Duration retryAfter
) {
    /**
     * Factory for a denied result.
     * Why a factory method and not a constructor call at the call site?
     *   The combination allowed=false, remaining=0 is always paired.
     *   Encoding that relationship in a named factory prevents inconsistency.
     */
    public static RateLimitResult denied(Instant resetAt, Duration retryAfter) {
        return new RateLimitResult(false, 0, resetAt, retryAfter);
    }

    /**
     * Factory for an allowed result.
     * retryAfter is always ZERO when allowed — the caller need not wait.
     */
    public static RateLimitResult allowed(int remaining, Instant resetAt) {
        return new RateLimitResult(true, remaining, resetAt, Duration.ZERO);
    }
}
