package com.lab.ratelimiter.infrastructure.config;

import com.lab.ratelimiter.application.service.RateLimitUseCase;
import com.lab.ratelimiter.domain.model.RateLimiterAlgorithm;
import com.lab.ratelimiter.domain.port.RateLimitStore;
import com.lab.ratelimiter.domain.service.SlidingWindowRateLimiter;
import com.lab.ratelimiter.domain.service.TokenBucketRateLimiter;
import com.lab.ratelimiter.infrastructure.redis.RedisRateLimitStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

/**
 * Pattern: Factory / Composition Root — assembles the object graph for the rate limiter.
 * OOP principle: Dependency Inversion — domain objects receive interfaces; wiring happens here.
 *
 * Why a @Configuration class instead of @Component + @Autowired everywhere?
 *   Explicit wiring is readable: you can see the full graph in one place.
 *   No field injection surprises — every dependency is visible at construction time.
 *   Makes it trivial to swap implementations (e.g., replace Redis with an in-memory store for local dev).
 *
 * Why Map<Algorithm, Service> instead of two separate beans?
 *   The use case selects a strategy by algorithm enum — O(1) Map lookup.
 *   Adding a third algorithm = one more Map.put() here, zero changes to RateLimitUseCase.
 *   OOP principle: Open/Closed — open for extension, closed for modification.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public RateLimitStore rateLimitStore(StringRedisTemplate redis) {
        return new RedisRateLimitStore(redis);
    }

    @Bean
    public RateLimitUseCase rateLimitUseCase(RateLimitStore store) {
        return new RateLimitUseCase(Map.of(
                RateLimiterAlgorithm.TOKEN_BUCKET,   new TokenBucketRateLimiter(store),
                RateLimiterAlgorithm.SLIDING_WINDOW, new SlidingWindowRateLimiter(store)
        ));
    }
}
