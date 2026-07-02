package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

public interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getPaymentMethod(String subscriptionId);
}
