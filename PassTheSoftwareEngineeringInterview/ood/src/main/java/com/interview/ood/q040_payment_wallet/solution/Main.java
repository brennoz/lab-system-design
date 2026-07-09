package com.interview.ood.q040_payment_wallet.solution;

import java.math.BigDecimal;
import java.util.UUID;

// Wiring (what Spring IoC does in prod):
//   @Bean InMemoryWalletRepository → replaced by JpaWalletRepository (Postgres + @Version)
//   @Bean WalletService(walletRepo)
//
// In prod (multi-pod):
//   Two ECS/EKS pods both receive debit £80 on the same wallet (balance=£100).
//   One succeeds (CAS: version 0→1). The other hits OptimisticLockException.
//   API Gateway returns 200 to first caller, 409 to second. Balance never goes negative.
//
// In prod (DynamoDB):
//   UpdateItem with ConditionExpression: "version = :expected"
//   ConditionalCheckFailedException → service catches → throws OptimisticLockException → 409.
public class Main {

    public static void main(String[] args) {
        WalletRepository repo    = new InMemoryWalletRepository();
        WalletService    service = new WalletService(repo);

        // Create wallet for customer roberto
        Wallet wallet = service.createWallet("roberto");
        System.out.println("=== Wallet created ===");
        System.out.println("WalletId : " + wallet.walletId());
        System.out.println("Balance  : £" + wallet.balance());
        System.out.println("Version  : " + wallet.version());

        // Credit £500
        String creditTxId = UUID.randomUUID().toString();
        service.credit(wallet.walletId(), new BigDecimal("500.00"), creditTxId);
        System.out.println("\n=== After credit £500 ===");
        System.out.println("Balance : £" + service.getBalance(wallet.walletId()));

        // Debit £80 — succeeds
        String debitTxId1 = UUID.randomUUID().toString();
        WalletTransaction tx1 = service.debit(wallet.walletId(), new BigDecimal("80.00"), debitTxId1);
        System.out.println("\n=== After debit £80 ===");
        System.out.println("TxId    : " + tx1.transactionId());
        System.out.println("Balance : £" + service.getBalance(wallet.walletId()));

        // Simulate optimistic lock conflict:
        // Read wallet at version=2, then manually commit a competing update to bump version.
        // Then attempt debit — CAS should fail.
        System.out.println("\n=== Simulating optimistic lock conflict ===");
        Wallet staleView = repo.findById(wallet.walletId()).get();  // version=2
        // Another "pod" commits a debit of £1, bumping version to 3
        Wallet competingUpdate = staleView.withBalance(staleView.balance().subtract(new BigDecimal("1.00")));
        repo.updateIfVersionMatches(competingUpdate);  // version 2→3

        // Now try to debit using the stale version=2 read — should fail
        // (We test this via WalletService indirectly; here we show the repo-level CAS)
        Wallet staleCopy = new Wallet(staleView.walletId(), staleView.ownerId(),
                staleView.balance().subtract(new BigDecimal("200.00")), staleView.version() + 1);
        boolean casResult = repo.updateIfVersionMatches(staleCopy);
        System.out.println("CAS with stale version: " + casResult + " (expected: false)");

        // Idempotency demo — replay same credit twice
        System.out.println("\n=== Idempotency demo: replay same transactionId ===");
        try {
            service.credit(wallet.walletId(), new BigDecimal("500.00"), creditTxId); // same id
        } catch (DuplicateTransactionException e) {
            System.out.println("Caught (expected): " + e.getMessage());
        }

        // Insufficient funds
        System.out.println("\n=== InsufficientFundsException demo ===");
        try {
            service.debit(wallet.walletId(), new BigDecimal("99999.00"), UUID.randomUUID().toString());
        } catch (InsufficientFundsException e) {
            System.out.println("Caught (expected): " + e.getMessage());
        }

        System.out.println("\n=== Final balance ===");
        System.out.println("Balance : £" + service.getBalance(wallet.walletId()));
    }
}
