package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// Demonstrates ALL FIVE CF methods:
//   thenCombine  — parallel fetch of plan + paymentMethod per subscription
//   thenCompose  — flattening the async charge call
//   thenApply    — wrapping charge result in RenewalResult.success()
//   exceptionally — per-item failure isolation (NOT on the outer batch)
//   allOf        — waiting for ALL per-item pipelines to complete
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

        // Java: Stream.map() here transforms each subscriptionId into a CF<RenewalResult>.
        //       All CFs start immediately — no blocking, no waiting.
        //       .toList() collects ALL CFs into a list before any join.
        // WHY collect first: if we called allOf() inside the stream, it would not have
        //     all futures yet. Collecting first ensures all per-item pipelines are in flight.
        List<CompletableFuture<RenewalResult>> futures = subscriptionIds.stream()
            .map(this::renewOne)  // each call returns CF<RenewalResult> immediately
            .toList();

        // CF: allOf() waits for EVERY CF in the list to complete — success OR failure.
        //     Because renewOne() handles failures with exceptionally(), every CF in 'futures'
        //     will complete normally (never exceptionally) — allOf() will always succeed.
        // Java: futures.toArray(new CompletableFuture[0]) — the zero-length array is a
        //       type hint for toArray(); Java uses it to determine the array type at runtime.
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored ->
                // CF: join() is safe here — allOf() guarantees every future is done.
                futures.stream()
                    .map(CompletableFuture::join)  // method reference — same as cf -> cf.join()
                    .toList()
            );
    }

    private CompletableFuture<RenewalResult> renewOne(String subscriptionId) {

        // Java: local record (Java 16+) — a record defined inside a method.
        //       Useful for intermediate types that don't belong in the public API.
        //       PlanAndPayment holds both fetched objects so we can pass them to charge().
        record PlanAndPayment(SubscriptionPlan plan, PaymentMethod paymentMethod) {}

        // Both fetches are INDEPENDENT for the same subscriptionId.
        CompletableFuture<SubscriptionPlan> planCf = planService.getPlan(subscriptionId);
        CompletableFuture<PaymentMethod>    pmCf   = paymentMethodService.getPaymentMethod(subscriptionId);

        return planCf
            // CF: thenCombine(other, fn) — waits for BOTH planCf AND pmCf to complete,
            //     then passes both results to fn. The two fetches run in parallel.
            // WHY thenCombine here (not allOf): we have exactly 2 independent CFs and need
            //     both values in the next step. thenCombine is ergonomic for exactly 2.
            //     allOf would also work but requires join() to extract the values.
            .thenCombine(pmCf, PlanAndPayment::new)  // CF<SubscriptionPlan> + CF<PaymentMethod> → CF<PlanAndPayment>

            // CF: thenCompose — chargeService.charge() returns CF<ChargeResult>.
            //     If we used thenApply here, the return type would be CF<CF<ChargeResult>>.
            //     thenCompose "flattens" this: the inner CF replaces the outer, giving CF<ChargeResult>.
            .thenCompose(pp -> chargeService.charge(pp.plan(), pp.paymentMethod()))  // CF<PlanAndPayment> → CF<ChargeResult>

            // CF: thenApply — sync wrap of successful charge into RenewalResult.success().
            .thenApply(charge -> RenewalResult.success(subscriptionId, charge))  // CF<ChargeResult> → CF<RenewalResult>

            // CF: exceptionally — catches any exception in the pipeline ABOVE this line.
            //     Returns a completed-normally CF<RenewalResult> with a failed result.
            // WHY PER-ITEM (not on the outer allOf):
            //     If exceptionally were on the allOf(), a single charge failure would produce
            //     ONE RenewalResult covering the whole batch — wrong.
            //     Here, each subscription's CF handles its own failure independently.
            //     The batch always gets back a full list, regardless of individual failures.
            // Java: exceptionally receives CompletionException (a CF wrapper) when the upstream
            //       CF failed. Unwrap to get the original cause's message so callers see the
            //       actual error text, not "java.lang.RuntimeException: ..." prefixed by wrapper.
            .exceptionally(ex -> {
                Throwable cause = (ex instanceof CompletionException && ex.getCause() != null)
                    ? ex.getCause() : ex;
                return RenewalResult.failed(subscriptionId, cause.getMessage());
            });
    }
}
