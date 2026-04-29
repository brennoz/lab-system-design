package com.lab.ratelimiter.domain;

import com.lab.ratelimiter.domain.port.RateLimitStore;
import com.lab.ratelimiter.domain.service.TokenBucketRateLimiter;
import com.lab.ratelimiter.fixtures.RateLimitFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TokenBucketRateLimiter domain service.
 * No Spring context, no Redis — Mockito mocks the RateLimitStore port.
 *
 * Pattern tested: Strategy (Token Bucket), Port (RateLimitStore mocked at boundary).
 * OOP tested: Dependency Inversion (depends on port, not Redis).
 *
 * Why mock RateLimitStore and not use a real Redis?
 *   Unit tests must be fast (< 10ms) and have no external dependencies.
 *   We're testing the ALGORITHM LOGIC here, not Redis connectivity.
 *   Integration tests (separate class) will test the real Redis path.
 *   This is the Test Pyramid: many fast unit tests, fewer slow integration tests.
 *
 * Data structure in tests: long counter returned by the mock.
 *   We simulate "what Redis would return" by controlling incrementAndGet() return value.
 */
@ExtendWith(MockitoExtension.class)
class TokenBucketRateLimiterTest {

    @Mock
    RateLimitStore store;

    TokenBucketRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new TokenBucketRateLimiter(store);
    }

    @Test
    void should_allow_first_request() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.tokenBucketLimit(); // max=5
        when(store.incrementAndGet(eq(key), any(Duration.class))).thenReturn(1L);

        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remaining()).isEqualTo(4); // 5 - 1
    }

    @Test
    void should_allow_request_at_exact_limit() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.tokenBucketLimit(); // max=5
        when(store.incrementAndGet(eq(key), any(Duration.class))).thenReturn(5L); // 5th request

        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isTrue();
        assertThat(result.remaining()).isEqualTo(0);
    }

    @Test
    void should_deny_request_exceeding_limit() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.tokenBucketLimit(); // max=5
        when(store.incrementAndGet(eq(key), any(Duration.class))).thenReturn(6L); // 6th request

        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isFalse();
        assertThat(result.remaining()).isEqualTo(0);
        assertThat(result.retryAfter()).isPositive(); // must tell client when to retry
    }

    @Test
    void should_include_reset_time_in_result() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.tokenBucketLimit();
        when(store.incrementAndGet(eq(key), any(Duration.class))).thenReturn(1L);

        var result = limiter.check(key, limit);

        assertThat(result.resetAt()).isNotNull();
        // resetAt must be in the future
        assertThat(result.resetAt()).isAfter(java.time.Instant.now());
    }

    @Test
    void should_call_store_with_correct_window_duration() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.tokenBucketLimit(); // window = 10s
        when(store.incrementAndGet(any(), any())).thenReturn(1L);

        limiter.check(key, limit);

        // Algorithm: Token Bucket uses the window as TTL for the counter
        verify(store).incrementAndGet(eq(key), eq(Duration.ofSeconds(10)));
    }

    @Test
    void denied_result_should_have_positive_retry_after() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.singleRequestLimit(); // max=1
        when(store.incrementAndGet(any(), any())).thenReturn(2L); // 2nd request denied

        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isFalse();
        // RFC 6585: Retry-After must be positive so clients know when to try again
        assertThat(result.retryAfter()).isPositive();
    }
}
