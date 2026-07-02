package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — billing address lives in the account service (remote call).
// KEY: this fetch is INDEPENDENT of the subscription fetch and payment method fetch.
//      All three can fire simultaneously — that's the whole point of this exercise.
public interface BillingAddressService {
    CompletableFuture<BillingAddress> getBillingAddress(String accountId);
}
