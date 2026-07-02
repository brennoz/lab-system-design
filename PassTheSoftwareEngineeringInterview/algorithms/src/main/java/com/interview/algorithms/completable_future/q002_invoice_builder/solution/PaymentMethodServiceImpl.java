package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// CF: supplyAsync — payment method fetch fires in parallel with subscription and address.
// In production: calls payment vault service (Stripe, Adyen, etc.) over HTTP.
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final ExecutorService executor;
    private final long delayMs;

    public PaymentMethodServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<PaymentMethod> getDefaultPaymentMethod(String accountId) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            return new PaymentMethod(accountId, "CARD", "4242");
        }, executor);
    }
}
