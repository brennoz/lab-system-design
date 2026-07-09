package com.interview.ood.q040_payment_wallet.solution;

import java.util.Optional;

// Port (Hexagonal Architecture) — domain interface; knows nothing about JPA, DynamoDB, or Redis.
// Implementations (Adapters):
//   InMemoryWalletRepository — used in tests and this exercise
//   JpaWalletRepository      — prod, backed by PostgreSQL with @Version on entity
//   DynamoWalletRepository   — prod alternative, uses ConditionExpression for CAS
public interface WalletRepository {
    void save(Wallet wallet);
    Optional<Wallet> findById(WalletId id);

    // CAS (Compare-And-Swap): update only if wallet.version() matches stored version.
    // Returns true on success, false on version mismatch (concurrent update detected).
    // In JPA: implemented automatically by @Version + optimistic lock mode.
    // In DynamoDB: ConditionExpression = "version = :v" → ConditionalCheckFailedException on mismatch.
    boolean updateIfVersionMatches(Wallet wallet);

    // Idempotency store: has this transactionId already been processed?
    // In prod: separate DynamoDB table with TTL (30 days) for idempotency keys.
    boolean transactionExists(String transactionId);

    // Append-only: never update or delete a recorded transaction.
    void recordTransaction(WalletTransaction tx);
}
