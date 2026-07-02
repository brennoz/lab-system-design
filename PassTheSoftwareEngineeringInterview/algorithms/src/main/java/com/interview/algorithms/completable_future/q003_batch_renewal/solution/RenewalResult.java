package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

// WHY Result type (not throwing): in batch renewal, ONE failed charge must NOT stop others.
//     Using a result type (success flag + optional error) lets the caller collect ALL
//     outcomes — including failures — without short-circuiting the batch.
// Pattern: Result / Either — a discriminated union of success / failure.
//          In Java: a record with a boolean flag and nullable fields is idiomatic.
public record RenewalResult(
    String subscriptionId,
    boolean success,
    ChargeResult chargeResult,   // non-null when success == true
    String errorMessage          // non-null when success == false
) {
    // WHY static factories: self-documenting call site; hides which fields are null.
    //     RenewalResult.success("sub-1", charge) vs new RenewalResult("sub-1", true, charge, null).
    public static RenewalResult success(String subscriptionId, ChargeResult chargeResult) {
        return new RenewalResult(subscriptionId, true, chargeResult, null);
    }

    public static RenewalResult failed(String subscriptionId, String errorMessage) {
        return new RenewalResult(subscriptionId, false, null, errorMessage);
    }
}
