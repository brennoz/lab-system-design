package com.lab.ratelimiter.domain.service;

import com.lab.ratelimiter.domain.model.RateLimit;
import com.lab.ratelimiter.domain.model.RateLimitKey;
import com.lab.ratelimiter.domain.model.RateLimitResult;

/**
 * Pattern: Strategy — defines the algorithm interface.
 * OOP principle: Polymorphism — callers invoke check() without knowing which algorithm runs.
 *   TokenBucketRateLimiter and SlidingWindowRateLimiter are the concrete strategies.
 *
 * Why an interface and not an abstract class?
 *   The strategies share NO common state or behaviour — only a contract.
 *   An abstract class would imply shared implementation, which is misleading.
 *   Interface = pure contract. Abstract class = partial implementation.
 *
 * Why not pass the store in the check() method signature?
 *   The store is an infrastructure dependency injected once at construction.
 *   Passing it per-call would expose infrastructure to the use case layer.
 */
public interface RateLimiterAlgorithmService {

    /**
     * Check whether this request is within the rate limit and update the counter.
     *
     * @param key   who is being rate-limited (e.g. "user:123")
     * @param limit the rule to apply (N requests per window)
     * @return result indicating whether the request is allowed and how many remain
     */
    RateLimitResult check(RateLimitKey key, RateLimit limit);
}
