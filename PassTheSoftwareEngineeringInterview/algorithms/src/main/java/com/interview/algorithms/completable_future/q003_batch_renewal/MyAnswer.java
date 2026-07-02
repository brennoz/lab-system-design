package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// Exercise 3: allOf + thenCombine + thenCompose + exceptionally (Hard)
// See concepts/completable_future/q003_batch_renewal/question.md for the full problem statement.
public class MyAnswer {

    private final PlanService planService;
    private final PaymentMethodService paymentMethodService;
    private final ChargeService chargeService;

    public MyAnswer(PlanService planService,
                    PaymentMethodService paymentMethodService,
                    ChargeService chargeService) {
        this.planService = planService;
        this.paymentMethodService = paymentMethodService;
        this.chargeService = chargeService;
    }

    /**
     * Renew a batch of subscriptions.
     *
     * For EACH subscriptionId:
     *   1. Fetch SubscriptionPlan and PaymentMethod IN PARALLEL  ← thenCombine
     *   2. Attempt charge using plan + payment method             ← thenCompose (charge returns CF)
     *   3. Wrap success in RenewalResult.success(...)             ← thenApply
     *   4. Isolate failure: failed charge → RenewalResult.failed(...) ← exceptionally
     *
     * Collect all per-item results with allOf.
     * Return CompletableFuture<List<RenewalResult>>.
     *
     * CRITICAL: one failed charge must NOT fail the whole batch.
     * The returned list always has the same size as the input list.
     *
     * CF methods to use: thenCombine, thenCompose, thenApply, exceptionally, allOf
     */
    public CompletableFuture<List<RenewalResult>> processRenewals(List<String> subscriptionIds) {
        throw new UnsupportedOperationException("Implement me — see question.md");
    }
}
