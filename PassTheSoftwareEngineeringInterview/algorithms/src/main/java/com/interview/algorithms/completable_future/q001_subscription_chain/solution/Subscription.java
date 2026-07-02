package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.math.BigDecimal;

// Java: Java 21 record — immutable data carrier.
//       Compiler auto-generates constructor, equals(), hashCode(), toString(),
//       and accessor methods (id(), accountId(), planName(), amount()).
// WHY records for domain models: immutability prevents accidental mutation when the same
//       object is passed across multiple async callbacks. Two threads sharing a mutable
//       Subscription could race on its fields — a record eliminates that risk entirely.
// ALT: A plain class with final fields and a constructor works but requires ~30 lines of
//       boilerplate. Records express the same idea in 1 line.
public record Subscription(
    String id,          // unique subscription identifier (e.g. "sub-abc-123")
    String accountId,   // foreign key to the Account that owns this subscription
    String planName,    // billing plan code, e.g. "ANNUAL", "MONTHLY"
    // Java: BigDecimal — NEVER use double or float for monetary values.
    //       double cannot represent 0.10 exactly (IEEE 754 binary fraction).
    //       At scale, rounding errors accumulate into real money discrepancies.
    BigDecimal amount   // full price before any discounts applied
) {}
