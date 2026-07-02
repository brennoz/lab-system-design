package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

// WHY a separate BillingAddress type: billing addresses live in the account service,
//     not the subscription service. Fetching it separately (not embedded in Subscription)
//     allows independent caching and independent service ownership.
// CF implication: BillingAddress fetch is INDEPENDENT of Subscription and PaymentMethod —
//     this is exactly what makes allOf() (not thenCompose) the right tool.
public record BillingAddress(
    String accountId, // links back to the account
    String street,
    String city,
    String postCode
) {}
