package com.lab.ratelimiter.domain;

import com.lab.ratelimiter.domain.model.RateLimit;
import com.lab.ratelimiter.domain.model.RateLimiterAlgorithm;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for RateLimit value object.
 * No Spring context — pure Java.
 *
 * OOP tested: Encapsulation (invalid rules rejected at construction),
 *             Immutability (records cannot be mutated after creation).
 */
class RateLimitTest {

    @Test
    void should_create_valid_rate_limit() {
        var limit = new RateLimit(100, Duration.ofSeconds(60), RateLimiterAlgorithm.TOKEN_BUCKET);
        assertThat(limit.maxRequests()).isEqualTo(100);
        assertThat(limit.window()).isEqualTo(Duration.ofSeconds(60));
        assertThat(limit.algorithm()).isEqualTo(RateLimiterAlgorithm.TOKEN_BUCKET);
    }

    @Test
    void should_reject_zero_max_requests() {
        // 0 requests per window means nothing is ever allowed — that is a misconfiguration
        assertThatThrownBy(() -> new RateLimit(0, Duration.ofSeconds(60), RateLimiterAlgorithm.TOKEN_BUCKET))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRequests");
    }

    @Test
    void should_reject_negative_max_requests() {
        assertThatThrownBy(() -> new RateLimit(-1, Duration.ofSeconds(60), RateLimiterAlgorithm.TOKEN_BUCKET))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxRequests");
    }

    @Test
    void should_reject_null_window() {
        assertThatThrownBy(() -> new RateLimit(100, null, RateLimiterAlgorithm.TOKEN_BUCKET))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("window");
    }

    @Test
    void should_reject_zero_duration_window() {
        // A zero window means the limit resets instantly — every request would be allowed
        assertThatThrownBy(() -> new RateLimit(100, Duration.ZERO, RateLimiterAlgorithm.TOKEN_BUCKET))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("window");
    }

    @Test
    void should_reject_null_algorithm() {
        assertThatThrownBy(() -> new RateLimit(100, Duration.ofSeconds(60), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("algorithm");
    }

    @Test
    void should_support_sliding_window_algorithm() {
        var limit = new RateLimit(100, Duration.ofMinutes(1), RateLimiterAlgorithm.SLIDING_WINDOW);
        assertThat(limit.algorithm()).isEqualTo(RateLimiterAlgorithm.SLIDING_WINDOW);
    }
}
