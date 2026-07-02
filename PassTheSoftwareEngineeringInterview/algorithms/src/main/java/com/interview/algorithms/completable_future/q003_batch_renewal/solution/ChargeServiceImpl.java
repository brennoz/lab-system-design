package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// CF: supplyAsync — charge call is the most latency-sensitive step (real payment processor call).
// In production: calls Stripe/Adyen API, receives a processor-assigned chargeId.
// See PlanServiceImpl for full CF/virtual-thread commentary — same pattern.
public class ChargeServiceImpl implements ChargeService {

    private final ExecutorService executor;
    private final long delayMs;

    public ChargeServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<ChargeResult> charge(SubscriptionPlan plan, PaymentMethod paymentMethod) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            // UUID simulates a processor-assigned charge ID (e.g. Stripe "ch_1ABC...")
            return new ChargeResult(plan.subscriptionId(), "ch_" + UUID.randomUUID(), plan.amount());
        }, executor);
    }
}
