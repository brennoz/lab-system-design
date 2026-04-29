package com.lab.ratelimiter.domain.model;

/**
 * Pattern: Value Object — immutable, equality by value, self-validating.
 * OOP principle: Encapsulation — the String format "type:id" is hidden inside;
 *   callers never construct raw strings and pass them where keys are expected.
 *
 * Why a record and not a plain String?
 *   String is an unbounded type — "user:123" and "apikey:abc" are the same type.
 *   A dedicated RateLimitKey makes method signatures self-documenting and lets
 *   the compiler catch "wrong key type" mistakes at compile time, not runtime.
 *
 * Data structure: String internally (single field).
 *   Why not two fields (type + id)?  The composite value is what Redis uses as
 *   a key. Keeping it pre-formatted avoids repeated string concatenation at call sites.
 *
 * STUB — validation not yet implemented. Tests will fail (red phase).
 */
public record RateLimitKey(String value) {

    // Compact constructor: guards the canonical constructor — no RateLimitKey with null/blank value can exist.
    // OOP principle: Fail Fast — invalid state is impossible, not just discouraged.
    public RateLimitKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("RateLimitKey value must not be null or blank");
        }
    }

    /**
     * Named factory validates each component before combining.
     * Why separate validation?  The error message names the bad field ("type" vs "identifier"),
     * which is more useful than "value must not be blank" when debugging a misconfigured client.
     */
    public static RateLimitKey of(String type, String identifier) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type must not be null or blank");
        }
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("identifier must not be null or blank");
        }
        return new RateLimitKey(type + ":" + identifier);
    }
}
