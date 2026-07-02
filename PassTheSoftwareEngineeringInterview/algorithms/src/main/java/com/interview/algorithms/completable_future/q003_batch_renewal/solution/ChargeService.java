package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — charging requires a call to the payment processor (Stripe, Adyen, etc.).
// WHY thenCompose (not thenApply) in the pipeline:
//     charge() returns CompletableFuture<ChargeResult>.
//     If we used thenApply to call charge(), we'd get CF<CF<ChargeResult>>.
//     thenCompose "flattens" the nested CF: CF<CF<ChargeResult>> → CF<ChargeResult>.
// ORDERING: charge MUST come after both plan and payment method are known.
//           plan + paymentMethod are fetched in parallel → combined → then charge.
public interface ChargeService {
    CompletableFuture<ChargeResult> charge(SubscriptionPlan plan, PaymentMethod paymentMethod);
}
