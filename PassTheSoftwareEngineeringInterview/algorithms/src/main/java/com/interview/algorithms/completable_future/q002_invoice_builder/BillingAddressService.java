package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

public interface BillingAddressService {
    CompletableFuture<BillingAddress> getBillingAddress(String accountId);
}
