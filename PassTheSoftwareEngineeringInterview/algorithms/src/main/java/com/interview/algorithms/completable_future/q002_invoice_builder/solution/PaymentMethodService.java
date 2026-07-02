package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — payment tokens stored in a payment vault service (remote call).
// WHY getDefaultPaymentMethod: accounts can have multiple methods; this returns
//     whichever is primary without the caller needing to know the method ID.
public interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getDefaultPaymentMethod(String accountId);
}
