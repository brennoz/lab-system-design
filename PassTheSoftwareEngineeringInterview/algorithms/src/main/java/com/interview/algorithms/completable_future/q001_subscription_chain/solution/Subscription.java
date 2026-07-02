package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.math.BigDecimal;

// Java: record — immutable value object; compiler generates constructor, equals, hashCode,
//       toString, and accessors (id(), accountId(), planName(), amount()).
// WHY BigDecimal for amount: double/float cannot represent 0.10 exactly (IEEE 754 binary
//     fraction); at billing scale, rounding errors accumulate into real money discrepancies.
public record Subscription(
    String id,
    String accountId,
    String planName,
    BigDecimal amount
) {}
