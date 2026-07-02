package com.interview.algorithms.completable_future.q001_subscription_chain;

public interface PricingService {
    // Synchronous — no CompletableFuture here
    DiscountedSubscription applyDiscount(Subscription subscription);
}
