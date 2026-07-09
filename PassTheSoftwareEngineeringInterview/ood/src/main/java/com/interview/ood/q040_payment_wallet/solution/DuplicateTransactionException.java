package com.interview.ood.q040_payment_wallet.solution;

// Thrown when idempotency check detects a replay of an already-processed transactionId.
// In prod: mapped to HTTP 200 (idempotent success) or HTTP 409 depending on API contract.
// Never HTTP 500 — this is an expected, handled case.
public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String transactionId) {
        super("Transaction already processed: " + transactionId);
    }
}
