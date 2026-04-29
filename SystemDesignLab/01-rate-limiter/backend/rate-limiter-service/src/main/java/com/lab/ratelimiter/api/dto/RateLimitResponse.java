package com.lab.ratelimiter.api.dto;

import com.lab.ratelimiter.domain.model.RateLimitResult;

import java.time.Instant;

/**
 * Pattern: DTO — maps domain result to HTTP response body.
 * OOP principle: Tell, Don't Ask — the controller reads this DTO to set headers; no raw inspection of domain internals.
 *
 * Standard rate-limit HTTP headers (de facto convention from GitHub, Stripe, etc.):
 *   X-RateLimit-Remaining  — how many requests the client has left
 *   X-RateLimit-Reset      — epoch second when the window resets
 *   Retry-After            — seconds until the client can retry (RFC 6585, only on 429)
 */
public record RateLimitResponse(
        boolean allowed,
        int     remaining,
        Instant resetAt,
        long    retryAfterSeconds
) {
    public static RateLimitResponse from(RateLimitResult result) {
        return new RateLimitResponse(
                result.allowed(),
                result.remaining(),
                result.resetAt(),
                result.retryAfter().toSeconds()
        );
    }
}
