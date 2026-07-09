package com.interview.ood.q040_payment_wallet.solution;

// Value Object — wraps a raw String id to prevent confusion with other String IDs.
// equals/hashCode from record guarantee two WalletIds with same value are equal.
public record WalletId(String value) {
    public static WalletId of(String value) { return new WalletId(value); }
    @Override public String toString() { return value; }
}
