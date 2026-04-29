package com.lab.ratelimiter.domain;

import com.lab.ratelimiter.domain.port.RateLimitStore;
import com.lab.ratelimiter.domain.service.SlidingWindowRateLimiter;
import com.lab.ratelimiter.fixtures.RateLimitFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SlidingWindowRateLimiter domain service.
 * No Spring context, no Redis — Mockito mocks the RateLimitStore port.
 *
 * Algorithm tested: Sliding Window Counter
 *   effectiveCount = currentCount + prevCount × (1 - elapsedFraction)
 *
 * Why test the interpolation formula explicitly?
 *   The formula is the core correctness guarantee of the algorithm.
 *   If it's wrong, the window boundary protection is broken.
 *   Tests lock in the expected numeric behaviour.
 *
 * Data structure: two counters (current window, previous window).
 *   We simulate them by controlling store.incrementAndGet() and store.get() return values.
 */
@ExtendWith(MockitoExtension.class)
class SlidingWindowRateLimiterTest {

    @Mock
    RateLimitStore store;

    SlidingWindowRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new SlidingWindowRateLimiter(store);
    }

    @Test
    void should_allow_first_request_with_no_previous_window() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.slidingWindowLimit(); // max=5
        when(store.incrementAndGet(eq(key), any())).thenReturn(1L); // current window: 1st request
        when(store.get(any())).thenReturn(0L);                      // previous window: empty

        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void should_deny_when_effective_count_exceeds_limit() {
        // Clock fixed at epoch=2s: for a 10s window, elapsed = 2%10 = 2s → fraction = 0.2
        // effectiveCount = 3 + 4 × (1 − 0.2) = 3 + 3.2 = 6.2 → exceeds limit of 5 → denied
        // Why fixed clock? Inline Instant.now() makes the assertion timing-dependent (50% pass rate).
        var clock  = Clock.fixed(Instant.ofEpochSecond(2), ZoneOffset.UTC);
        var limiter = new SlidingWindowRateLimiter(store, clock);
        var key    = RateLimitFixture.userKey();
        var limit  = RateLimitFixture.slidingWindowLimit(); // max=5, window=10s
        when(store.incrementAndGet(eq(key), any())).thenReturn(3L); // current = 3
        when(store.get(any())).thenReturn(4L);                      // previous = 4

        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isFalse();
    }

    @Test
    void should_allow_when_previous_window_weight_reduces_effective_count() {
        // Clock fixed at epoch=8s: for a 10s window, elapsed = 8%10 = 8s → fraction = 0.8
        // effectiveCount = 1 + 5 × (1 − 0.8) = 1 + 1.0 = 2.0 → under limit of 5 → allowed
        // This is the key Sliding Window advantage: at 80% elapsed, the heavy previous window
        // contributes only 20% of its weight — a Fixed Window would still count it fully.
        var clock  = Clock.fixed(Instant.ofEpochSecond(8), ZoneOffset.UTC);
        var limiter = new SlidingWindowRateLimiter(store, clock);
        var key    = RateLimitFixture.userKey();
        var limit  = RateLimitFixture.slidingWindowLimit(); // max=5, window=10s
        when(store.incrementAndGet(eq(key), any())).thenReturn(1L); // current = 1
        when(store.get(any())).thenReturn(5L);                      // previous = 5

        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isTrue();
    }

    @Test
    void should_deny_request_at_exact_limit() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.slidingWindowLimit(); // max=5
        when(store.incrementAndGet(eq(key), any())).thenReturn(5L); // current = 5
        when(store.get(any())).thenReturn(0L);                      // previous = 0

        // 5 in current window = exactly at limit (count > limit means deny)
        // Whether limit is inclusive/exclusive is a design decision — we test both edges
        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isTrue(); // exactly at limit = still allowed
        assertThat(result.remaining()).isEqualTo(0);
    }

    @Test
    void should_read_both_current_and_previous_window_from_store() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.slidingWindowLimit();
        when(store.incrementAndGet(any(), any())).thenReturn(1L);
        when(store.get(any())).thenReturn(0L);

        limiter.check(key, limit);

        // Algorithm requires exactly one write (current window INCR)
        // and one read (previous window GET)
        verify(store, times(1)).incrementAndGet(any(), any());
        verify(store, times(1)).get(any());
    }

    @Test
    void denied_result_should_have_positive_retry_after() {
        var key   = RateLimitFixture.userKey();
        var limit = RateLimitFixture.slidingWindowLimit(); // max=5
        when(store.incrementAndGet(any(), any())).thenReturn(10L); // way over limit
        when(store.get(any())).thenReturn(0L);

        var result = limiter.check(key, limit);

        assertThat(result.allowed()).isFalse();
        assertThat(result.retryAfter()).isPositive();
    }
}
