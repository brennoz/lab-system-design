package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — subscription lookup requires a remote call.
// WHY getSubscription (not findById): naming follows the query intent.
//     findById is typically a repository method; getSubscription is a service operation
//     that may apply business rules (e.g. check subscription is active) before returning.
public interface SubscriptionService {
    CompletableFuture<Subscription> getSubscription(String subscriptionId);
}
