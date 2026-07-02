package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

// RenewalResult wraps the outcome of renewing ONE subscription.
// WHY not throw on failure: in a batch renewal, one failed charge must NOT stop
//     the others. Using a result type (success flag + optional error) instead of
//     exceptions allows the caller to collect ALL outcomes, not just the first failure.
// Pattern: this is the Result/Either pattern — a discriminated union of success/failure.
//     In Haskell/Scala this would be Either<Error, ChargeResult>.
//     In Java, we model it as a record with a boolean flag and nullable fields.
public record RenewalResult(
    String subscriptionId,
    boolean success,
    ChargeResult chargeResult,  // non-null when success == true
    String errorMessage         // non-null when success == false
) {

    // Java: static factory methods on records are valid and preferred over 'new'.
    //       They communicate intent clearly at the call site.

    // WHY static factory over constructor: the constructor requires all 4 args every time.
    //     With factory methods, successful results never have to pass null for errorMessage,
    //     and failed results never have to pass null for chargeResult.
    //     The correct null is encapsulated in the factory — callers cannot get it wrong.
    public static RenewalResult success(String subscriptionId, ChargeResult chargeResult) {
        return new RenewalResult(subscriptionId, true, chargeResult, null);
    }

    public static RenewalResult failed(String subscriptionId, String errorMessage) {
        return new RenewalResult(subscriptionId, false, null, errorMessage);
    }
}
