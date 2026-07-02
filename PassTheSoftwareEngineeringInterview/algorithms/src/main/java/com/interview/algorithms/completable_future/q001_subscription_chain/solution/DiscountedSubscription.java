package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.math.BigDecimal;

// Java: Separate record for the discounted form of a subscription.
// WHY a distinct type: if we reused Subscription and just mutated the amount field,
//     callers could confuse a discounted amount for an original amount.
//     Distinct types make the compiler enforce correct usage — you cannot pass a
//     DiscountedSubscription where a Subscription is expected without an explicit conversion.
// DDD: This is a Value Object transformation — the same identity (same id, same accountId)
//      but a different representation after the pricing step.
public record DiscountedSubscription(
    String id,
    String accountId,
    String planName,
    BigDecimal discountedAmount  // price after discount; field name differs from Subscription.amount
                                 // to make the distinction explicit at the call site
) {}
