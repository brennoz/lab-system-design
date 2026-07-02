package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// Java: record with three String fields — no BigDecimal needed here,
//       Account is a reference object (billing metadata), not a financial value.
// WHY fetch Account separately from Subscription:
//     In a real Zuora-like billing system, subscriptions and accounts live in
//     different bounded contexts / microservices. Fetching them together (JOIN)
//     would couple the subscription service to the account service.
//     Fetching separately keeps services independent and allows independent scaling.
public record Account(
    String id,    // matches Subscription.accountId — the join key
    String name,  // display name for invoice header
    String email  // billing email for notifications
) {}
