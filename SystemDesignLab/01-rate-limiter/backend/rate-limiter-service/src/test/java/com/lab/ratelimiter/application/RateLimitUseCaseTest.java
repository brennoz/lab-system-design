package com.lab.ratelimiter.application;

import com.lab.ratelimiter.application.service.RateLimitUseCase;
import com.lab.ratelimiter.domain.model.RateLimitResult;
import com.lab.ratelimiter.domain.model.RateLimiterAlgorithm;
import com.lab.ratelimiter.domain.service.RateLimiterAlgorithmService;
import com.lab.ratelimiter.fixtures.RateLimitFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitUseCase application service.
 * No Spring context, no Redis — Mockito mocks the algorithm strategies.
 *
 * Pattern tested: Façade (single entry point), Strategy dispatch via Map
 * OOP tested: Dependency Inversion (use case depends on interfaces, not Redis)
 * Data structure tested: Map<Algorithm, Service> — O(1) strategy selection
 *
 * Why mock both strategies?
 *   We're testing the DISPATCH logic here (does TOKEN_BUCKET call the right service?).
 *   The algorithm logic itself is tested in TokenBucketRateLimiterTest and SlidingWindowRateLimiterTest.
 *   Mixing both would make failures ambiguous — is the dispatch wrong or the algorithm?
 */
@ExtendWith(MockitoExtension.class)
class RateLimitUseCaseTest {

    @Mock
    RateLimiterAlgorithmService tokenBucketService;

    @Mock
    RateLimiterAlgorithmService slidingWindowService;

    RateLimitUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RateLimitUseCase(Map.of(
                RateLimiterAlgorithm.TOKEN_BUCKET,   tokenBucketService,
                RateLimiterAlgorithm.SLIDING_WINDOW, slidingWindowService
        ));
    }

    @Test
    void should_delegate_to_token_bucket_strategy_when_algorithm_is_token_bucket() {
        var key    = RateLimitFixture.userKey();
        var limit  = RateLimitFixture.tokenBucketLimit(); // algorithm = TOKEN_BUCKET
        var expect = RateLimitResult.allowed(4, Instant.now().plusSeconds(10));
        when(tokenBucketService.check(eq(key), eq(limit))).thenReturn(expect);

        var result = useCase.check(key, limit);

        assertThat(result).isEqualTo(expect);
        verify(tokenBucketService, times(1)).check(eq(key), eq(limit));
        verifyNoInteractions(slidingWindowService);
    }

    @Test
    void should_delegate_to_sliding_window_strategy_when_algorithm_is_sliding_window() {
        var key    = RateLimitFixture.userKey();
        var limit  = RateLimitFixture.slidingWindowLimit(); // algorithm = SLIDING_WINDOW
        var expect = RateLimitResult.allowed(3, Instant.now().plusSeconds(10));
        when(slidingWindowService.check(eq(key), eq(limit))).thenReturn(expect);

        var result = useCase.check(key, limit);

        assertThat(result).isEqualTo(expect);
        verify(slidingWindowService, times(1)).check(eq(key), eq(limit));
        verifyNoInteractions(tokenBucketService);
    }

    @Test
    void should_propagate_denied_result_from_strategy() {
        var key    = RateLimitFixture.userKey();
        var limit  = RateLimitFixture.tokenBucketLimit();
        var denied = RateLimitResult.denied(Instant.now().plusSeconds(10), java.time.Duration.ofSeconds(5));
        when(tokenBucketService.check(any(), any())).thenReturn(denied);

        var result = useCase.check(key, limit);

        assertThat(result.allowed()).isFalse();
        assertThat(result.retryAfter()).isPositive();
    }

    @Test
    void should_throw_when_algorithm_is_not_registered() {
        // Use case built with no strategies — simulates missing registration
        var emptyUseCase = new RateLimitUseCase(Map.of());
        var key          = RateLimitFixture.userKey();
        var limit        = RateLimitFixture.tokenBucketLimit();

        // Algorithm not in Map → must fail loudly, not silently allow
        assertThatThrownBy(() -> emptyUseCase.check(key, limit))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TOKEN_BUCKET");
    }
}
