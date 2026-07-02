package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.util.concurrent.CompletableFuture;

public interface SubscriptionService {
    CompletableFuture<Subscription> findById(String id);
}
