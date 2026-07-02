package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// CF: supplyAsync — billing address fetch fires in parallel with subscription and payment.
// In production: calls address microservice over HTTP.
public class BillingAddressServiceImpl implements BillingAddressService {

    private final ExecutorService executor;
    private final long delayMs;

    public BillingAddressServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<BillingAddress> getBillingAddress(String accountId) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            return new BillingAddress(accountId, "1 Billing St", "London", "EC1A 1BB");
        }, executor);
    }
}
