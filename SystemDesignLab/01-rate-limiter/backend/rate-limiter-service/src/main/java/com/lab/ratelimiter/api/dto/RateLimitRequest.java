package com.lab.ratelimiter.api.dto;

import com.lab.ratelimiter.domain.model.RateLimiterAlgorithm;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Pattern: DTO (Data Transfer Object) — carries raw JSON input across the HTTP boundary.
 * OOP principle: Separation of Concerns — keeps JSON field names and validation annotations
 *   out of the domain model. The domain record RateLimit has no idea HTTP exists.
 *
 * Why a record?  DTOs are immutable once deserialized — a record enforces this for free.
 * Why @NotBlank / @Min here and not in the domain?
 *   Domain validation uses IllegalArgumentException (programmer error).
 *   HTTP boundary validation uses MethodArgumentNotValidException → 400 Bad Request (client error).
 *   Two different failure modes, two different validation layers.
 */
public record RateLimitRequest(
        @NotBlank String keyType,
        @NotBlank String keyIdentifier,
        @Min(1)   int    maxRequests,
        @Min(1)   long   windowSeconds,
        @NotNull  RateLimiterAlgorithm algorithm
) {}
