package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.math.BigDecimal;

// Returned by ChargeService on success. chargeId is the processor's reference
// (e.g. Stripe chargeId "ch_1ABC...") — persist this for refunds and dispute resolution.
public record ChargeResult(String subscriptionId, String chargeId, BigDecimal amount) {}
