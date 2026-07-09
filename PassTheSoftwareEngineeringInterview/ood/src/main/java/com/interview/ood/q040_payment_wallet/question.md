**Lessons:** [0002 Payment Wallet](../../../../../topics/ood/payment/0002-payment-wallet.html)

# q040 — Payment Wallet

**Category:** ood · **Topic:** payment · **Difficulty:** medium · **Time:** 20 min

## Problem

Design a `Wallet` system for a payment platform. A wallet holds a balance for one owner (e.g. a customer account). Multiple concurrent API requests may debit the same wallet simultaneously.

### Requirements

1. `Wallet` — aggregate root holding `walletId`, `ownerId`, `balance` (BigDecimal), `version` (int for optimistic locking)
2. `WalletRepository` (interface) — `save`, `findById`, `updateIfVersionMatches` (CAS), `transactionExists`, `recordTransaction`
3. `WalletService` — `createWallet`, `credit`, `debit` (with idempotency key), `getBalance`
4. `debit` must protect against concurrent overdraft using optimistic locking (version CAS)
5. `debit` must be idempotent: same `transactionId` twice → no double debit
6. Use `BigDecimal` for all monetary amounts — never `double` or `float`

### Skeleton description

- `WalletId` — value object wrapping a String id
- `TransactionType` — enum: `CREDIT`, `DEBIT`
- `Wallet` — Java record with `withBalance(BigDecimal)` returning new Wallet with version+1
- `WalletTransaction` — value object: transactionId, walletId, type, amount, createdAt
- Custom exceptions: `InsufficientFundsException`, `OptimisticLockException`, `DuplicateTransactionException`, `WalletNotFoundException`
- `WalletRepository` — interface (Port in hexagonal)
- `InMemoryWalletRepository` — adapter using HashMap; `updateIfVersionMatches` checks version before updating
- `WalletService` — constructor-injected repo; `debit` does: idempotency check → balance check → CAS update → record tx
- `Main` — demo: create wallet, credit, two concurrent debits (one wins, one gets OptimisticLockException)

### Rubric

**Pass requires:**
- [ ] `version` field present on Wallet, incremented on each update
- [ ] `updateIfVersionMatches` returns false on version mismatch (not exception)
- [ ] `debit` throws `InsufficientFundsException` before CAS attempt when balance insufficient
- [ ] `debit` idempotency check runs BEFORE balance check and CAS
- [ ] `BigDecimal` used everywhere (not double)
- [ ] `WalletRepository` is an interface (not a concrete class)
- [ ] Constructor injection in `WalletService` (no field injection)
- [ ] 3+ tests covering: happy-path credit, happy-path debit, optimistic lock conflict, duplicate transactionId

**Bonus:**
- [ ] `Main.java` demos two concurrent debit attempts
- [ ] Comments explain distributed/prod implications
- [ ] Credit also idempotent

### Folder structure

Write your attempt in `myanswer1/` package (`com.interview.ood.q040_payment_wallet.myanswer1`).
Write your tests in `src/test/.../q040_payment_wallet/myanswer1/WalletServiceTest.java`.
See `solution/` for reference implementation after attempt.
