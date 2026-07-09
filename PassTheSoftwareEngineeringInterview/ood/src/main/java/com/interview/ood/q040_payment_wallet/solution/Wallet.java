package com.interview.ood.q040_payment_wallet.solution;

import java.math.BigDecimal;

// Pattern: Aggregate Root — Wallet owns its balance; no external class may mutate balance directly.
// Immutable record: all "mutation" returns a new Wallet instance (functional style).
// version field: simulates JPA @Version for optimistic locking.
//   In prod (JPA): @Version int version; JPA auto-increments and checks on UPDATE.
//   In prod (DynamoDB): ConditionExpression "version = :expected" on PutItem.
//
// Why BigDecimal not double?
//   double 0.1 + 0.2 = 0.30000000000000004 — IEEE 754 floating-point rounding.
//   BigDecimal("0.10").add(BigDecimal("0.20")) = 0.30 — exact decimal arithmetic.
//   FCA / PSD2 mandate exact monetary representation; double fails audit.
public record Wallet(WalletId walletId, String ownerId, BigDecimal balance, int version) {

    // Returns a new Wallet with updated balance and incremented version.
    // Caller passes this to WalletRepository.updateIfVersionMatches(updated).
    public Wallet withBalance(BigDecimal newBalance) {
        return new Wallet(walletId, ownerId, newBalance, version + 1);
    }

    // Guard — keeps the business rule close to the data it guards.
    public boolean hasSufficientFunds(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
}
