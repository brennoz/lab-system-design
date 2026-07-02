package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.math.BigDecimal;

public record Invoice(Subscription subscription,
                      BillingAddress billingAddress,
                      PaymentMethod paymentMethod) {
    public BigDecimal amount() {
        return subscription.amount();
    }
}
