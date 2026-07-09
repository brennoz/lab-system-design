package com.interview.ood.q040_payment_wallet.solution;

// Thrown when CAS (Compare-And-Swap) version check fails — another thread committed first.
// In prod: caller catches this and either retries (if client context allows) or returns HTTP 409.
// Retry strategy: exponential backoff, max 3 attempts. If still failing → 409 to client.
// JPA equivalent: javax.persistence.OptimisticLockException (thrown by EntityManager on flush).
public class OptimisticLockException extends RuntimeException {
    public OptimisticLockException(String message) {
        super(message);
    }
}
