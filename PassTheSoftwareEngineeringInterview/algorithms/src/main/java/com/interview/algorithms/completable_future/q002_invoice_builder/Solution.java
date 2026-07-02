package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — do not read before attempting MyAnswer.java
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
        // Fire all three simultaneously — none depends on any other
        CompletableFuture<Subscription>    subCf  = subscriptionService.getSubscription(subscriptionId);
        CompletableFuture<BillingAddress>  addrCf = billingAddressService.getBillingAddress(accountId);
        CompletableFuture<PaymentMethod>   pmCf   = paymentMethodService.getDefaultPaymentMethod(accountId);

        // allOf waits for ALL to complete, returns CF<Void>
        // join() inside thenApply is SAFE here: allOf guarantees all three are done before thenApply runs
        return CompletableFuture.allOf(subCf, addrCf, pmCf)
            .thenApply(ignored -> new Invoice(subCf.join(), addrCf.join(), pmCf.join()));

        // Alternative with thenCombine (works for exactly 2, needs nesting for 3+):
        // return subCf.thenCombine(addrCf, (sub, addr) -> Map.entry(sub, addr))
        //     .thenCombine(pmCf, (pair, pm) -> new Invoice(pair.getKey(), pair.getValue(), pm));
        // allOf is cleaner when combining 3+ independent CFs.
    }
}
