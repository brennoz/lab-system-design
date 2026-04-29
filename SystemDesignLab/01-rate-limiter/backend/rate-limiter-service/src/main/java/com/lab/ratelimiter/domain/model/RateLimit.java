package com.lab.ratelimiter.domain.model;

import java.time.Duration;

/**
 * Pattern: Value Object — immutable rule describing what is allowed.
 * OOP principle: Encapsulation — invalid rules cannot exist; validation at construction.
 *
 * Represents: "allow at most N requests per window using this algorithm".
 * Example: RateLimit(100, Duration.ofSeconds(60), TOKEN_BUCKET)
 *
 * Why Duration for window instead of int seconds?
 *   int is ambiguous: is 60 seconds, milliseconds, or minutes?
 *   Duration.ofSeconds(60) is self-documenting — no unit confusion.
 *   This eliminates a whole class of bugs (unit mismatch) at the API level.
 *
 * Why int for maxRequests instead of long?
 *   Rate limits in practice never exceed Integer.MAX_VALUE (2.1 billion) per window.
 *   int signals "small positive number" to the reader. long signals "could be huge".
 *
 * STUB — validation not yet implemented. Tests will fail (red phase).
 */
public record RateLimit(int maxRequests, Duration window, RateLimiterAlgorithm algorithm) {

    // Compact constructor: enforces the invariant that a RateLimit is always a valid rule.
    // OOP principle: Encapsulation — invalid configuration cannot reach the domain layer.
    public RateLimit {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be > 0, got: " + maxRequests);
        }
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be a positive duration, got: " + window);
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("algorithm must not be null");
        }
    }
}
