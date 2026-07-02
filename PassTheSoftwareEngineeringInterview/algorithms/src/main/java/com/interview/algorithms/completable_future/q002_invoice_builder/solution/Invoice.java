package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.math.BigDecimal;

// Invoice is an aggregate — it brings together data from three separate services
// (subscription service, billing address service, payment method service).
// Java: compact accessor method `amount()` is defined inline in the record body.
//       Records can have methods — they just cannot have mutable instance fields.
// WHY the amount() accessor: avoids callers having to chain invoice.subscription().amount()
//     in every place they need the total. Encapsulates the navigation.
public record Invoice(
    Subscription subscription,
    BillingAddress billingAddress,
    PaymentMethod paymentMethod
) {
    // Java: compact method inside a record body — valid Java 16+ syntax.
    // Delegates to the subscription's amount field.
    public BigDecimal amount() {
        return subscription.amount();
    }
}
