package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — plan data comes from the subscription/billing service (network call).
// WHY per-subscription: each subscription may be on a different plan with a different
//     price. Batch renewal must fetch the current plan for each ID individually.
public interface PlanService {
    CompletableFuture<SubscriptionPlan> getPlan(String subscriptionId);
}
