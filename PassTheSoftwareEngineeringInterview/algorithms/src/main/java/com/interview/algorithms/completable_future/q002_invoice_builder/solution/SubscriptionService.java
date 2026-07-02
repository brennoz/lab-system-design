package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — subscription lookup requires a remote call.
// WHY getSubscription (vs findById): service-layer naming signals that business rules
//     may apply (e.g., check subscription is active) before returning.
public interface SubscriptionService {
    CompletableFuture<Subscription> getSubscription(String subscriptionId);
}
