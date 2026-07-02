package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — plan data comes from the billing service (network call per subscription).
// KEY: PlanService and PaymentMethodService calls for the SAME subscriptionId are
//      INDEPENDENT of each other → fire them in parallel with thenCombine.
public interface PlanService {
    CompletableFuture<SubscriptionPlan> getPlan(String subscriptionId);
}
