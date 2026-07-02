package com.interview.algorithms.completable_future.q001_subscription_chain;

public record EnrichedSubscription(DiscountedSubscription subscription, Account account) {}
