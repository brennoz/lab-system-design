package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — payment method stored in a payment vault service (remote call).
// WHY getDefaultPaymentMethod: accounts can have multiple payment methods;
//     this returns whichever is set as primary without requiring the caller to know the ID.
public interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getDefaultPaymentMethod(String accountId);
}
