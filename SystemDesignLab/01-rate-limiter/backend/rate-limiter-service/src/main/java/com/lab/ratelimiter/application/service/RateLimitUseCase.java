package com.lab.ratelimiter.application.service;

import com.lab.ratelimiter.domain.model.RateLimit;
import com.lab.ratelimiter.domain.model.RateLimitKey;
import com.lab.ratelimiter.domain.model.RateLimitResult;
import com.lab.ratelimiter.domain.model.RateLimiterAlgorithm;
import com.lab.ratelimiter.domain.service.RateLimiterAlgorithmService;

import java.util.Map;

/**
 * Pattern: Façade — single entry point; callers need not know which algorithm runs
 * OOP: Dependency Inversion — depends on RateLimiterAlgorithmService interface, not concrete classes
 * Data structure: Map<Algorithm, Service> — O(1) strategy lookup by enum key
 * Why Map not if/else: Open/Closed — adding a third algorithm = add one Map entry, zero if changes
 */
public class RateLimitUseCase {

    private final Map<RateLimiterAlgorithm, RateLimiterAlgorithmService> strategies;

    public RateLimitUseCase(Map<RateLimiterAlgorithm, RateLimiterAlgorithmService> strategies) {
        this.strategies = strategies;
    }

    public RateLimitResult check(RateLimitKey key, RateLimit limit) {
        // Data structure: Map — O(1) strategy lookup by enum key.
        // Why Map<Enum, Service> not if/else?  Open/Closed Principle: a third algorithm
        // is one Map.put() in the Spring config — zero changes to this class.
        RateLimiterAlgorithmService strategy = strategies.get(limit.algorithm());
        if (strategy == null) {
            // Fail loudly: a missing registration is a wiring bug, not a runtime condition.
            // Including the algorithm name lets the developer find the misconfiguration immediately.
            throw new IllegalArgumentException(
                    "No strategy registered for algorithm: " + limit.algorithm());
        }
        return strategy.check(key, limit);
    }
}
