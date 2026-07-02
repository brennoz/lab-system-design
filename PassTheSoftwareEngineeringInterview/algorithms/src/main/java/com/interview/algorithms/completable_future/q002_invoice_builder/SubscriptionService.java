package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

public interface SubscriptionService {
    CompletableFuture<Subscription> getSubscription(String subscriptionId);
}
