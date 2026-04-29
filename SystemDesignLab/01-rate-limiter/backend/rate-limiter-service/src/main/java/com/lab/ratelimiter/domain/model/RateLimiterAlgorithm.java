package com.lab.ratelimiter.domain.model;

/**
 * Pattern: Strategy (enumerated) — algorithm selection as a first-class domain concept.
 * OOP principle: Open/Closed — adding a new algorithm means adding an enum constant
 *   and a new implementation class; existing code is not modified.
 *
 * Why an enum and not a String?
 *   Strings allow invalid values ("TOKENBUCKT", "sliding_window").
 *   An enum restricts choices to valid values at compile time.
 *   Switch expressions on enums are exhaustive — the compiler warns if a case is missing.
 */
public enum RateLimiterAlgorithm {

    /**
     * Token Bucket: allows bursts up to bucket capacity, average rate bounded by refill rate.
     * Best for: general-purpose APIs where occasional bursts are acceptable.
     * Complexity: O(1) per request.
     */
    TOKEN_BUCKET,

    /**
     * Sliding Window Counter: interpolates between current and previous window counts.
     * Best for: billing APIs where boundary accuracy matters more than burst allowance.
     * Complexity: O(1) per request.
     * More accurate than Fixed Window at window boundaries.
     */
    SLIDING_WINDOW
}
