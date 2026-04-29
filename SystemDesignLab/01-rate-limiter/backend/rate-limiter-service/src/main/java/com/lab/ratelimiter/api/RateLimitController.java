package com.lab.ratelimiter.api;

import com.lab.ratelimiter.api.dto.RateLimitRequest;
import com.lab.ratelimiter.api.dto.RateLimitResponse;
import com.lab.ratelimiter.application.service.RateLimitUseCase;
import com.lab.ratelimiter.domain.model.RateLimit;
import com.lab.ratelimiter.domain.model.RateLimitKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Pattern: Adapter (Hexagonal) — HTTP adapter; translates HTTP ↔ domain objects.
 * OOP principle: Single Responsibility — only mapping and HTTP status decisions here;
 *   no business logic crosses into this class.
 *
 * Why POST and not GET?
 *   Checking a rate limit is a write operation (it increments a counter).
 *   GET implies idempotency — calling it twice would not change state.
 *   A counter INCR is not idempotent, so POST is semantically correct.
 *
 * HTTP status conventions:
 *   200 OK    — request allowed, counter incremented
 *   429 Too Many Requests — limit exceeded (RFC 6585)
 */
@RestController
@RequestMapping("/api/v1/rate-limit")
@Tag(name = "Rate Limiter", description = "Check whether a request is allowed under the configured rate limit")
public class RateLimitController {

    private final RateLimitUseCase useCase;

    public RateLimitController(RateLimitUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/check")
    @Operation(summary = "Check a rate limit", description = "Increments the counter for the given key and returns allowed/denied with remaining quota")
    public ResponseEntity<RateLimitResponse> check(@Valid @RequestBody RateLimitRequest request) {
        var key    = RateLimitKey.of(request.keyType(), request.keyIdentifier());
        var limit  = new RateLimit(request.maxRequests(), Duration.ofSeconds(request.windowSeconds()), request.algorithm());
        var result = useCase.check(key, limit);
        var body   = RateLimitResponse.from(result);

        // HTTP 429 if denied; 200 if allowed. Retry-After header follows RFC 6585.
        if (!result.allowed()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("Retry-After", String.valueOf(body.retryAfterSeconds()))
                    .header("X-RateLimit-Remaining", "0")
                    .header("X-RateLimit-Reset", String.valueOf(result.resetAt().getEpochSecond()))
                    .body(body);
        }

        return ResponseEntity.ok()
                .header("X-RateLimit-Remaining", String.valueOf(body.remaining()))
                .header("X-RateLimit-Reset", String.valueOf(result.resetAt().getEpochSecond()))
                .body(body);
    }
}
