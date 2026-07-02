package com.interview.algorithms.completable_future.q003_batch_renewal;

public record RenewalResult(String subscriptionId,
                             boolean success,
                             ChargeResult chargeResult,
                             String errorMessage) {

    public static RenewalResult success(String subscriptionId, ChargeResult chargeResult) {
        return new RenewalResult(subscriptionId, true, chargeResult, null);
    }

    public static RenewalResult failed(String subscriptionId, String errorMessage) {
        return new RenewalResult(subscriptionId, false, null, errorMessage);
    }
}
