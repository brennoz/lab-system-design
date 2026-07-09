package com.interview.ood.q040_payment_wallet.solution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Pattern: Unit test with mocked repository — exercises WalletService in isolation.
// Each test covers exactly one behaviour; no test overlaps another.
// Test count: 4 — happy-path credit, happy-path debit, InsufficientFunds, OptimisticLock.
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepo;

    @InjectMocks
    private WalletService walletService;

    // ── credit records transaction and updates balance ────────────────────────────
    @Test
    void credit_records_transaction_and_updates_balance() {
        WalletId walletId = WalletId.of("w-001");
        String txId = "tx-credit-001";
        Wallet existingWallet = new Wallet(walletId, "owner-1", BigDecimal.ZERO, 0);

        when(walletRepo.transactionExists(txId)).thenReturn(false);
        when(walletRepo.findById(walletId)).thenReturn(Optional.of(existingWallet));
        when(walletRepo.updateIfVersionMatches(any())).thenReturn(true);

        WalletTransaction result = walletService.credit(walletId, new BigDecimal("100.00"), txId);

        assertEquals(TransactionType.CREDIT, result.type());
        assertEquals(new BigDecimal("100.00"), result.amount());
        assertEquals(walletId, result.walletId());

        // Verify CAS called with updated balance and incremented version
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepo).updateIfVersionMatches(walletCaptor.capture());
        Wallet updatedWallet = walletCaptor.getValue();
        assertEquals(new BigDecimal("100.00"), updatedWallet.balance());
        assertEquals(1, updatedWallet.version());

        // Verify transaction recorded exactly once
        verify(walletRepo, times(1)).recordTransaction(any(WalletTransaction.class));
    }

    // ── debit succeeds when sufficient funds and no version conflict ──────────────
    @Test
    void debit_succeeds_when_sufficient_funds_and_no_version_conflict() {
        WalletId walletId = WalletId.of("w-001");
        String txId = "tx-debit-001";
        Wallet existingWallet = new Wallet(walletId, "owner-1", new BigDecimal("100.00"), 0);

        when(walletRepo.transactionExists(txId)).thenReturn(false);
        when(walletRepo.findById(walletId)).thenReturn(Optional.of(existingWallet));
        when(walletRepo.updateIfVersionMatches(any())).thenReturn(true);

        WalletTransaction result = walletService.debit(walletId, new BigDecimal("30.00"), txId);

        assertEquals(TransactionType.DEBIT, result.type());
        assertEquals(new BigDecimal("30.00"), result.amount());

        // CAS called with balance=£70 and version=1
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepo).updateIfVersionMatches(walletCaptor.capture());
        Wallet updatedWallet = walletCaptor.getValue();
        assertEquals(new BigDecimal("70.00"), updatedWallet.balance());
        assertEquals(1, updatedWallet.version());

        verify(walletRepo, times(1)).recordTransaction(any(WalletTransaction.class));
    }

    // ── debit throws InsufficientFundsException before CAS attempt ───────────────
    @Test
    void debit_throws_insufficient_funds_before_cas_attempt() {
        WalletId walletId = WalletId.of("w-001");
        String txId = "tx-debit-insufficient";
        Wallet existingWallet = new Wallet(walletId, "owner-1", new BigDecimal("10.00"), 0);

        when(walletRepo.transactionExists(txId)).thenReturn(false);
        when(walletRepo.findById(walletId)).thenReturn(Optional.of(existingWallet));

        assertThrows(InsufficientFundsException.class,
                () -> walletService.debit(walletId, new BigDecimal("50.00"), txId));

        // CAS must NOT be attempted — reject before the write
        verify(walletRepo, never()).updateIfVersionMatches(any());
        verify(walletRepo, never()).recordTransaction(any());
    }

    // ── debit throws OptimisticLockException when CAS fails ──────────────────────
    @Test
    void debit_throws_optimistic_lock_when_cas_fails() {
        WalletId walletId = WalletId.of("w-001");
        String txId = "tx-debit-conflict";
        Wallet existingWallet = new Wallet(walletId, "owner-1", new BigDecimal("100.00"), 5);

        when(walletRepo.transactionExists(txId)).thenReturn(false);
        when(walletRepo.findById(walletId)).thenReturn(Optional.of(existingWallet));
        // Simulate another pod committed first → version mismatch
        when(walletRepo.updateIfVersionMatches(any())).thenReturn(false);

        assertThrows(OptimisticLockException.class,
                () -> walletService.debit(walletId, new BigDecimal("20.00"), txId));

        // Transaction must NOT be recorded — CAS failed, no phantom debit
        verify(walletRepo, never()).recordTransaction(any());
    }
}
