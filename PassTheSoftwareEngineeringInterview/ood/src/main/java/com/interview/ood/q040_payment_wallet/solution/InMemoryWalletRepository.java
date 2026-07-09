package com.interview.ood.q040_payment_wallet.solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Adapter — in-memory implementation of WalletRepository for tests and demos.
// Not thread-safe by design (exercise focus is on service-level CAS logic).
// In prod: replaced by JpaWalletRepository; @Version on the JPA entity + @Transactional
//   on WalletService ensures the CAS check and UPDATE are atomic within one DB transaction.
//
// Data structures:
//   wallets: HashMap — O(1) get/put by WalletId. No ordering needed.
//   processedTxIds: HashSet — O(1) idempotency lookup. Equivalent to a DB unique index.
//   transactions: HashMap<walletId, List<WalletTransaction>> — append-only ledger per wallet.
public class InMemoryWalletRepository implements WalletRepository {

    private final Map<WalletId, Wallet>                      wallets        = new HashMap<>();
    private final Set<String>                                processedTxIds = new HashSet<>();
    private final Map<WalletId, List<WalletTransaction>>     transactions   = new HashMap<>();

    @Override
    public void save(Wallet wallet) {
        wallets.put(wallet.walletId(), wallet);
    }

    @Override
    public Optional<Wallet> findById(WalletId id) {
        return Optional.ofNullable(wallets.get(id));
    }

    // CAS: only commit if the version we're writing matches the version we read.
    // Simulates the atomicity that a DB row lock or DynamoDB ConditionExpression provides.
    @Override
    public boolean updateIfVersionMatches(Wallet updated) {
        Wallet current = wallets.get(updated.walletId());
        if (current == null) return false;
        // Version mismatch → another writer committed between our read and this write.
        if (current.version() != updated.version() - 1) return false;
        wallets.put(updated.walletId(), updated);
        return true;
    }

    @Override
    public boolean transactionExists(String transactionId) {
        return processedTxIds.contains(transactionId);
    }

    @Override
    public void recordTransaction(WalletTransaction tx) {
        processedTxIds.add(tx.transactionId());
        transactions.computeIfAbsent(tx.walletId(), k -> new ArrayList<>()).add(tx);
    }
}
