package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — charging calls the payment processor (Stripe, Adyen, etc.) over the network.
// WHY thenCompose (not thenApply) when calling charge():
//     charge() returns CF<ChargeResult>. thenApply would give CF<CF<ChargeResult>>.
//     thenCompose flattens: CF<CF<ChargeResult>> → CF<ChargeResult>.
// ORDERING: charge() must come AFTER both plan and payment method are known (thenCompose
//     is chained after thenCombine that waits for both).
public interface ChargeService {
    CompletableFuture<ChargeResult> charge(SubscriptionPlan plan, PaymentMethod paymentMethod);
}
