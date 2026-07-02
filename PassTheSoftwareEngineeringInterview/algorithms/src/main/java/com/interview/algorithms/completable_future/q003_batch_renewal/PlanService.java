package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.util.concurrent.CompletableFuture;

public interface PlanService {
    CompletableFuture<SubscriptionPlan> getPlan(String subscriptionId);
}
