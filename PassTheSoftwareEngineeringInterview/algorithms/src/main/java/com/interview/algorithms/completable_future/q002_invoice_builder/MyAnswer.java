package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

// Exercise 2: thenCombine / allOf — parallel independent fetches
// See algorithms/completable_future/q002_invoice_builder/question.md for the full problem statement.
public class MyAnswer {

    private final SubscriptionService subscriptionService;
    private final BillingAddressService billingAddressService;
    private final PaymentMethodService paymentMethodService;

    public MyAnswer(SubscriptionService subscriptionService,
                    BillingAddressService billingAddressService,
                    PaymentMethodService paymentMethodService) {
        this.subscriptionService = subscriptionService;
        this.billingAddressService = billingAddressService;
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * Build an Invoice by fetching three independent pieces of data in parallel:
     * - Subscription (by subscriptionId)
     * - BillingAddress (by accountId)
     * - PaymentMethod (by accountId)
     *
     * All three fetches are INDEPENDENT — no result depends on another.
     * Fire all three before waiting for any.
     *
     * CF methods to use: allOf (or thenCombine chained twice)
     *
     * @return CompletableFuture<Invoice>
     */
    public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId) {
        throw new UnsupportedOperationException("Implement me — see question.md");
    }
}
