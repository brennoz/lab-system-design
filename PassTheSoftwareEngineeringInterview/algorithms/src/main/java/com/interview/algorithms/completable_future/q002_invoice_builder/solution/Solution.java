package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// Demonstrates: parallel independent CF fetches with allOf().
// The key insight: when N async calls are INDEPENDENT (no result depends on another),
// fire ALL of them before waiting for ANY. allOf() coordinates completion.
public class Solution {

    private final SubscriptionService subscriptionService;
    private final BillingAddressService billingAddressService;
    private final PaymentMethodService paymentMethodService;

    public Solution(SubscriptionService subscriptionService,
                    BillingAddressService billingAddressService,
                    PaymentMethodService paymentMethodService) {
        this.subscriptionService = subscriptionService;
        this.billingAddressService = billingAddressService;
        this.paymentMethodService = paymentMethodService;
    }

    public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId) {

        // WHY fire all three BEFORE waiting for any:
        //     Sequential: sub(300ms) → addr(200ms) → pm(150ms) = 650ms total
        //     Parallel:   all three in flight simultaneously        = ~300ms total (longest wins)
        // These three calls are INDEPENDENT — no result depends on another.
        CompletableFuture<Subscription>   subCf  = subscriptionService.getSubscription(subscriptionId);
        CompletableFuture<BillingAddress> addrCf = billingAddressService.getBillingAddress(accountId);
        CompletableFuture<PaymentMethod>  pmCf   = paymentMethodService.getDefaultPaymentMethod(accountId);

        // CF: allOf(cf1, cf2, cf3) returns a CF<Void> that completes when ALL input CFs complete.
        //     If any CF fails, allOf completes exceptionally immediately.
        //     allOf does NOT carry the results — access them via the original CF references.
        return CompletableFuture.allOf(subCf, addrCf, pmCf)
            .thenApply(ignored -> {
                // CF: join() is SAFE here because allOf() guarantees that subCf, addrCf, pmCf
                //     are ALL done before this thenApply callback runs.
                //     join() on a completed CF returns immediately (no blocking, no waiting).
                //     Never call join() before allOf() — that would block a thread.
                // WHY join() and not get(): join() throws CompletionException (unchecked),
                //     not ExecutionException (checked). Cleaner inside lambda bodies
                //     where checked exceptions must be caught explicitly.
                return new Invoice(subCf.join(), addrCf.join(), pmCf.join());
            });

        // ALT: thenCombine for exactly 2 CFs:
        //   subCf.thenCombine(addrCf, (sub, addr) -> new Object[]{sub, addr})
        //        .thenCombine(pmCf, (pair, pm) -> new Invoice((Subscription)pair[0], (BillingAddress)pair[1], pm))
        //   Works but: (a) ugly intermediate type, (b) doesn't parallelise well beyond 2.
        //   allOf is the idiomatic choice for 3+ independent CFs.
    }
}
