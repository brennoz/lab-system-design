package com.interview.ood.q040_payment_wallet.solution;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Pattern: Service Layer — owns all wallet business logic.
// All dependencies via constructor injection: testable without Spring, final fields are thread-safe.
//
// @Transactional in prod (Spring + JPA):
//   credit/debit span multiple repo calls → must commit atomically.
//   Without @Transactional: balance updated → crash → transaction not recorded → audit gap.
//
// Multi-pod race condition (2 pods, same wallet):
//   Both read balance=£100, version=5. Pod A debit £80 succeeds (version→6).
//   Pod B debit £80: updateIfVersionMatches finds version mismatch → throws OptimisticLockException.
//   Caller either retries (if retry is safe) or returns HTTP 409. Balance stays £20. Safe.
//
// In prod (AWS):
//   DynamoDB: ConditionExpression "version = :v" → ConditionalCheckFailedException on conflict.
//   RDS (Aurora): @Version on JPA entity + SELECT FOR UPDATE alternative for high-contention paths.
//   Redis: WATCH/MULTI/EXEC for non-JPA stores (e.g. wallet cache in ElastiCache).
public class WalletService {

    private final WalletRepository walletRepo;

    public WalletService(WalletRepository walletRepo) {
        this.walletRepo = walletRepo;
    }

    public Wallet createWallet(String ownerId) {
        WalletId id = WalletId.of(UUID.randomUUID().toString());
        // BigDecimal.ZERO: start at exactly 0.00 — no floating-point noise.
        Wallet wallet = new Wallet(id, ownerId, BigDecimal.ZERO, 0);
        walletRepo.save(wallet);
        return wallet;
    }

    // Credit — also idempotent: same transactionId twice → DuplicateTransactionException.
    // In prod: merchant settlement credits come with a unique settlement reference (= transactionId).
    public WalletTransaction credit(WalletId walletId, BigDecimal amount, String transactionId) {
        if (walletRepo.transactionExists(transactionId)) {
            throw new DuplicateTransactionException(transactionId);
        }
        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        Wallet updated = wallet.withBalance(wallet.balance().add(amount));
        if (!walletRepo.updateIfVersionMatches(updated)) {
            throw new OptimisticLockException("Concurrent credit conflict on wallet " + walletId);
        }
        WalletTransaction tx = new WalletTransaction(transactionId, walletId,
                TransactionType.CREDIT, amount, Instant.now());
        walletRepo.recordTransaction(tx);
        return tx;
    }

    // Debit — critical path.
    // Order matters: idempotency check FIRST (cheapest), then balance check, then CAS.
    // If CAS fails, no transaction is recorded — safe to retry.
    public WalletTransaction debit(WalletId walletId, BigDecimal amount, String transactionId) {
        // 1. Idempotency: never double-debit on network retry.
        if (walletRepo.transactionExists(transactionId)) {
            throw new DuplicateTransactionException(transactionId);
        }

        // 2. Load current state.
        Wallet wallet = walletRepo.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        // 3. Business rule: reject before CAS attempt (avoid wasted DB write).
        if (!wallet.hasSufficientFunds(amount)) {
            throw new InsufficientFundsException(walletId, amount, wallet.balance());
        }

        // 4. CAS: only commit if no concurrent writer changed version since our read.
        Wallet updated = wallet.withBalance(wallet.balance().subtract(amount));
        if (!walletRepo.updateIfVersionMatches(updated)) {
            // Another pod committed first. Caller retries or returns 409.
            throw new OptimisticLockException("Concurrent debit conflict on wallet " + walletId);
        }

        // 5. Record transaction AFTER successful CAS — no phantom DEBIT if CAS fails.
        WalletTransaction tx = new WalletTransaction(transactionId, walletId,
                TransactionType.DEBIT, amount, Instant.now());
        walletRepo.recordTransaction(tx);
        return tx;
    }

    public BigDecimal getBalance(WalletId walletId) {
        return walletRepo.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId))
                .balance();
    }
}
