package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — payment tokens are stored in a payment vault (external service).
// KEY: PlanService and PaymentMethodService calls for the SAME subscriptionId
//      are INDEPENDENT of each other — they can run in parallel using thenCombine.
public interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getPaymentMethod(String subscriptionId);
}
