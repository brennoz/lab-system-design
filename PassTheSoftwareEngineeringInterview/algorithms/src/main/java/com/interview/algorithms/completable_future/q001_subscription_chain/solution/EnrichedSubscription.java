package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// Output model — combines the discounted price + account metadata.
// Pattern: Assembler / Enrichment — fetch from multiple sources, produce one composite view.
// Java: records compose well; EnrichedSubscription contains two other records.
//       Equality is by fields — two EnrichedSubscriptions with identical data are equal.
public record EnrichedSubscription(
    DiscountedSubscription subscription,
    Account account
) {}
