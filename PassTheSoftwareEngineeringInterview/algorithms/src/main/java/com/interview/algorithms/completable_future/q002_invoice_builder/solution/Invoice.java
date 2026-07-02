package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.math.BigDecimal;

// Aggregate output: combines data from three independent services.
// Java: records can have methods in their body — they just cannot have mutable instance fields.
//       amount() is a custom accessor that delegates to subscription.amount().
// WHY amount() on Invoice: avoids callers writing invoice.subscription().amount() everywhere.
//     Encapsulates the navigation path. If the model changes, only this one method changes.
public record Invoice(Subscription subscription, BillingAddress billingAddress, PaymentMethod paymentMethod) {

    public BigDecimal amount() {
        return subscription.amount();
    }
}
