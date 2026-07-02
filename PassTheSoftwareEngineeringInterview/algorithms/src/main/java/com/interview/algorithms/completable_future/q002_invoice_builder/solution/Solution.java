package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// Demonstrates: allOf() for N independent parallel fetches + completeOnTimeout per service.
public class Solution {

    private final SubscriptionService subscriptionService;
    private final BillingAddressService billingAddressService;
    private final PaymentMethodService paymentMethodService;
    // timeoutMs is a constructor param so tests can pass 10L (fast) and Main.java passes 500L.
    private final long timeoutMs;

    public Solution(SubscriptionService subscriptionService,
                    BillingAddressService billingAddressService,
                    PaymentMethodService paymentMethodService,
                    long timeoutMs) {
        this.subscriptionService = subscriptionService;
        this.billingAddressService = billingAddressService;
        this.paymentMethodService = paymentMethodService;
        this.timeoutMs = timeoutMs;
    }

    public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId) {
        // WHY fire all three before waiting for any:
        //     Sequential: sub(200ms) → addr(180ms) → pm(150ms) = 530ms total
        //     Parallel:   all three simultaneously             ≈ 200ms total (slowest wins)
        // These three calls are INDEPENDENT — no result depends on another's value.
        CompletableFuture<Subscription> subCf = subscriptionService.getSubscription(subscriptionId)
            // CF: completeOnTimeout — if the CF does not complete within timeoutMs,
            //     it is completed NORMALLY with the fallback value (Subscription.empty()).
            //     The CF never completes exceptionally due to timeout; allOf() always succeeds.
            // WHY completeOnTimeout (not orTimeout): orTimeout throws TimeoutException,
            //     which would fail the whole invoice build. completeOnTimeout lets the invoice
            //     be built with degraded data — better for user experience.
            .completeOnTimeout(Subscription.empty(), timeoutMs, MILLISECONDS);

        CompletableFuture<BillingAddress> addrCf = billingAddressService.getBillingAddress(accountId)
            .completeOnTimeout(BillingAddress.unknown(), timeoutMs, MILLISECONDS);

        CompletableFuture<PaymentMethod> pmCf = paymentMethodService.getDefaultPaymentMethod(accountId)
            .completeOnTimeout(PaymentMethod.unknown(), timeoutMs, MILLISECONDS);

        // CF: allOf(cf1, cf2, cf3) — returns CF<Void> that completes when ALL input CFs complete.
        //     Because we used completeOnTimeout, all three CFs always complete normally.
        //     allOf does NOT carry the results — access them via the original CF references.
        return CompletableFuture.allOf(subCf, addrCf, pmCf)
            .thenApply(ignored -> {
                // CF: join() is SAFE here — allOf() guarantees all three are done before
                //     this callback runs. join() on a completed CF returns immediately.
                //     WHY join() not get(): join() throws CompletionException (unchecked),
                //     cleaner inside lambdas where checked exceptions need explicit handling.
                return new Invoice(subCf.join(), addrCf.join(), pmCf.join());
            });
    }
}
