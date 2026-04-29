package com.lab.ratelimiter.api;

import com.lab.ratelimiter.api.dto.RateLimitRequest;
import com.lab.ratelimiter.application.service.RateLimitUseCase;
import com.lab.ratelimiter.domain.model.RateLimitResult;
import com.lab.ratelimiter.domain.model.RateLimiterAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for RateLimitController — no Spring context, no HTTP, pure Mockito.
 * Tests: correct HTTP status code for allowed (200) and denied (429).
 */
@ExtendWith(MockitoExtension.class)
class RateLimitControllerTest {

    @Mock
    RateLimitUseCase useCase;

    @InjectMocks
    RateLimitController controller;

    @Test
    void should_return_200_when_request_is_allowed() {
        when(useCase.check(any(), any()))
                .thenReturn(RateLimitResult.allowed(4, Instant.now().plusSeconds(10)));

        var response = controller.check(new RateLimitRequest("user", "123", 5, 10, RateLimiterAlgorithm.TOKEN_BUCKET));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().allowed()).isTrue();
    }

    @Test
    void should_return_429_when_request_is_denied() {
        when(useCase.check(any(), any()))
                .thenReturn(RateLimitResult.denied(Instant.now().plusSeconds(10), Duration.ofSeconds(10)));

        var response = controller.check(new RateLimitRequest("user", "123", 5, 10, RateLimiterAlgorithm.TOKEN_BUCKET));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody().allowed()).isFalse();
        assertThat(response.getHeaders().getFirst("Retry-After")).isNotNull();
    }
}
