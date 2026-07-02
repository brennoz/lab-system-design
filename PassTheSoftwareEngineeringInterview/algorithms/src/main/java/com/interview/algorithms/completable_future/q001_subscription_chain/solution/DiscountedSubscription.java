package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.math.BigDecimal;

// WHY separate type: the compiler prevents callers from confusing a discounted amount
//     with the original. You cannot pass DiscountedSubscription where Subscription is
//     expected — the distinction is enforced at compile time, not by convention.
public record DiscountedSubscription(
    String id,
    String accountId,
    String planName,
    BigDecimal discountedAmount
) {}
