package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.math.BigDecimal;

// WHY SubscriptionPlan (not Subscription): in billing, the "plan" is the contract that
//     defines how much to charge at renewal. Separating plan from subscription makes it
//     explicit that we're looking at the RENEWAL AMOUNT, not the original purchase price.
public record SubscriptionPlan(String subscriptionId, String planName, BigDecimal amount) {}
