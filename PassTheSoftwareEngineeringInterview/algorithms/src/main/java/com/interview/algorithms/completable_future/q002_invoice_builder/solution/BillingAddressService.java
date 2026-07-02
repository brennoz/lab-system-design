package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — billing address lives in the account service (remote call).
// KEY: this fetch is INDEPENDENT of subscription and payment method — all three can
//      fire simultaneously. That's what allOf() coordinates.
public interface BillingAddressService {
    CompletableFuture<BillingAddress> getBillingAddress(String accountId);
}
