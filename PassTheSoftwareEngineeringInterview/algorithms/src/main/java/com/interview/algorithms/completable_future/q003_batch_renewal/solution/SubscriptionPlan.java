package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.math.BigDecimal;

// WHY SubscriptionPlan (not Subscription): in a billing context, a plan is the
//     contract details (what the customer signed up for and how much to charge).
//     Distinguishing Plan from Subscription avoids the ambiguity of "which amount?" —
//     the plan's amount IS what gets charged at renewal.
public record SubscriptionPlan(
    String subscriptionId,   // which subscription this plan belongs to
    String planName,         // e.g. "MONTHLY", "ANNUAL"
    BigDecimal amount        // Java: BigDecimal — amount to charge at renewal
) {}
