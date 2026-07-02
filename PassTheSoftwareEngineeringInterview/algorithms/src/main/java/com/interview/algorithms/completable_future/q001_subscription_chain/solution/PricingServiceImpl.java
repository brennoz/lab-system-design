package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.math.BigDecimal;

// Synchronous — no executor needed. Pure in-memory calculation.
// Spring Boot: @Service — instantiated by Spring, injected into Solution as PricingService.
public class PricingServiceImpl implements PricingService {

    // 10% discount applied unconditionally for simplicity
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    @Override
    public DiscountedSubscription applyDiscount(Subscription subscription) {
        BigDecimal discounted = subscription.amount()
            .subtract(subscription.amount().multiply(DISCOUNT_RATE));
        return new DiscountedSubscription(
            subscription.id(),
            subscription.accountId(),
            subscription.planName(),
            discounted
        );
    }
}
