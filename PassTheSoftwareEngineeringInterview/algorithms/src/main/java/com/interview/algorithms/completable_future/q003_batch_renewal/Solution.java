package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — do not read before attempting MyAnswer.java
public class Solution {

    private final PlanService planService;
    private final PaymentMethodService paymentMethodService;
    private final ChargeService chargeService;

    public Solution(PlanService planService,
                    PaymentMethodService paymentMethodService,
                    ChargeService chargeService) {
        this.planService = planService;
        this.paymentMethodService = paymentMethodService;
        this.chargeService = chargeService;
    }

    public CompletableFuture<List<RenewalResult>> processRenewals(List<String> subscriptionIds) {
        // Build one CF<RenewalResult> per subscription
        List<CompletableFuture<RenewalResult>> futures = subscriptionIds.stream()
            .map(this::renewOne)
            .toList(); // collect ALL CFs first — so all start before any join

        // allOf waits for every CF to complete (success or failure)
        // join() inside thenApply is safe: allOf guarantees all are done
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }

    private CompletableFuture<RenewalResult> renewOne(String subscriptionId) {
        // Intermediate record to hold plan + payment method together
        record PlanAndPayment(SubscriptionPlan plan, PaymentMethod paymentMethod) {}

        CompletableFuture<SubscriptionPlan> planCf = planService.getPlan(subscriptionId);
        CompletableFuture<PaymentMethod>    pmCf   = paymentMethodService.getPaymentMethod(subscriptionId);

        return planCf
            .thenCombine(pmCf, PlanAndPayment::new)                               // parallel fetch — combine when both done
            .thenCompose(pp -> chargeService.charge(pp.plan(), pp.paymentMethod())) // charge returns CF — thenCompose flattens
            .thenApply(charge -> RenewalResult.success(subscriptionId, charge))    // wrap success
            .exceptionally(ex -> RenewalResult.failed(subscriptionId, ex.getMessage())); // isolate failure per item
        // exceptionally on the PER-ITEM CF means one charge failure → that item is FAILED, not the whole batch
    }
}
