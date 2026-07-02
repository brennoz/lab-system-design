package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.math.BigDecimal;

// Returned by ChargeService when a charge succeeds.
// WHY chargeId: the payment processor (Stripe, Adyen, etc.) returns a charge ID
//     that can be used for refunds, disputes, and audit trail.
//     Always persist this — losing it means you cannot refund programmatically.
public record ChargeResult(
    String subscriptionId,  // identifies which renewal this charge belongs to
    String chargeId,        // processor-assigned charge identifier (e.g. "ch_1ABC...")
    BigDecimal amount       // actual amount charged (should match plan amount)
) {}
