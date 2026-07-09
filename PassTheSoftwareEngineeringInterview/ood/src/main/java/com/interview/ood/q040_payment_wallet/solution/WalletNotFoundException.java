package com.interview.ood.q040_payment_wallet.solution;

// Maps to HTTP 404 in prod. Non-retryable.
public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(WalletId walletId) {
        super("Wallet not found: " + walletId);
    }
}
