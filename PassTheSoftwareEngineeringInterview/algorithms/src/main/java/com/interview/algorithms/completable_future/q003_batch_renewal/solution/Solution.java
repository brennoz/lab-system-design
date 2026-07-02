package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// Demonstrates ALL FIVE CF methods in one exercise:
//   thenCombine  — parallel plan + payment fetch per subscription
//   thenCompose  — async charge call (flattens CF<CF<ChargeResult>>)
//   thenApply    — wrap success in RenewalResult
//   exceptionally — per-item failure isolation (NOT on the outer batch)
//   allOf        — wait for all per-item pipelines to finish
//   orTimeout    — hard timeout on charge; TimeoutException caught by exceptionally
public class Solution {

    private final PlanService planService;
    private final PaymentMethodService paymentMethodService;
    private final ChargeService chargeService;
    private final long timeoutMs;   // per-charge timeout; tests pass 10L, Main.java passes 2000L

    public Solution(PlanService planService,
                    PaymentMethodService paymentMethodService,
                    ChargeService chargeService,
                    long timeoutMs) {
        this.planService = planService;
        this.paymentMethodService = paymentMethodService;
        this.chargeService = chargeService;
        this.timeoutMs = timeoutMs;
    }

    public CompletableFuture<List<RenewalResult>> processRenewals(List<String> subscriptionIds) {
        // Start ALL per-item pipelines immediately — none waits for another.
        // Java: .toList() collects ALL CFs into a list before any join.
        //       Collecting first ensures all pipelines are in flight simultaneously.
        List<CompletableFuture<RenewalResult>> futures = subscriptionIds.stream()
            .map(this::renewOne)   // each call returns CF<RenewalResult> immediately
            .toList();

        // CF: allOf() waits for EVERY CF in the list to complete.
        //     Because renewOne() handles failures with exceptionally(), every CF always
        //     completes NORMALLY — allOf() itself never completes exceptionally.
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored ->
                // CF: join() is safe — allOf() guarantees every future is done.
                futures.stream()
                    .map(CompletableFuture::join)
                    .toList()
            );
    }

    private CompletableFuture<RenewalResult> renewOne(String subscriptionId) {
        // Java 16+: local record — an intermediate type scoped to this method.
        //           Avoids adding PlanAndPayment to the public API.
        record PlanAndPayment(SubscriptionPlan plan, PaymentMethod pm) {}

        // Both fetches are INDEPENDENT for the same subscriptionId → fire in parallel.
        return planService.getPlan(subscriptionId)
            // CF: thenCombine(other, fn) — waits for BOTH CFs, passes both results to fn.
            //     Ergonomic for exactly 2 independent CFs. allOf() also works but requires join().
            .thenCombine(paymentMethodService.getPaymentMethod(subscriptionId),
                         PlanAndPayment::new)

            // CF: thenCompose — charge() returns CF<ChargeResult>.
            //     thenApply would give CF<CF<ChargeResult>> — double-wrapped.
            //     thenCompose flattens: CF<CF<ChargeResult>> → CF<ChargeResult>.
            .thenCompose(pp ->
                chargeService.charge(pp.plan(), pp.pm())
                    // CF: orTimeout — if charge doesn't complete in timeoutMs,
                    //     the CF completes exceptionally with TimeoutException.
                    //     WHY orTimeout (not completeOnTimeout): a timed-out charge is a
                    //     genuine failure — we do NOT want to silently create a fake ChargeResult.
                    //     The TimeoutException is caught by exceptionally() below.
                    .orTimeout(timeoutMs, MILLISECONDS))

            // Sync wrap of successful charge into output type
            .thenApply(charge -> RenewalResult.success(subscriptionId, charge))

            // CF: exceptionally — catches ANY exception in the pipeline above.
            //     This includes: card decline, network error, TimeoutException.
            //     Returns a completed-normally CF — never propagates the exception to allOf().
            // WHY PER-ITEM (not on the outer allOf):
            //     If exceptionally were on allOf(), one failure would cover the WHOLE batch.
            //     Here: each subscription's failure is isolated to its own RenewalResult.
            .exceptionally(ex -> RenewalResult.failed(subscriptionId, extractMessage(ex)));
    }

    // Unwrap CompletionException: when thenCompose's inner CF fails, the exception is
    // re-wrapped in CompletionException before reaching exceptionally().
    // TimeoutException has no message → fall back to class name.
    private static String extractMessage(Throwable ex) {
        Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
        String msg = cause.getMessage();
        return (msg != null) ? msg : cause.getClass().getSimpleName();
    }
}
