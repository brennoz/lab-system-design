package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// CF: supplyAsync — payment method fetch fires in parallel with plan fetch for same subscription.
// In production: calls payment vault service (Stripe, Adyen) to resolve the stored token.
// See PlanServiceImpl for full CF/virtual-thread commentary — same pattern.
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final ExecutorService executor;
    private final long delayMs;

    public PaymentMethodServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<PaymentMethod> getPaymentMethod(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            return new PaymentMethod(subscriptionId, "CARD", "tok-" + subscriptionId);
        }, executor);
    }
}
