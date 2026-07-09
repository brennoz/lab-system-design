package com.interview.ood.q040_payment_wallet.solution;

import java.math.BigDecimal;

// Domain exception — maps to HTTP 422 Unprocessable Entity in prod.
// Not a retry-able error: retrying will not fix an empty wallet.
// In Kafka consumer: this goes to DLQ, not retry topic.
public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(WalletId walletId, BigDecimal requested, BigDecimal available) {
        super(String.format("Wallet %s: insufficient funds — requested %s, available %s",
                walletId, requested, available));
    }
}
