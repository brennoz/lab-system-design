package com.interview.ood.q044_atm_system.solution;

import java.math.BigDecimal;

// Runnable demo — not a test. Walks the same "user journey" a live interviewer would want
// talked through: insert card → attempt a transaction before authenticating (rejected) →
// authenticate → withdraw → check balance → deposit → attempt an overdraft (rejected) →
// eject card → attempt another transaction (rejected, session reset).
public class AtmMain {

    public static void main(String[] args) {
        Account account = new Account("ACC-1", new BigDecimal("100.00"));
        AtmSession session = new AtmSession();
        AtmService service = new AtmService(session);

        System.out.println("=== Insert card, attempt a withdrawal before authenticating ===");
        session.insertCard(account);
        System.out.println("Session status: " + session.getStatus());
        try {
            service.submitCommand(new WithdrawCommand(account, new BigDecimal("10.00")));
        } catch (IllegalStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Authenticate, then withdraw ===");
        session.authenticate();
        System.out.println("Session status: " + session.getStatus());
        service.submitCommand(new WithdrawCommand(account, new BigDecimal("30.00")));
        System.out.println("Balance after withdrawing 30.00: " + account.getBalance());

        System.out.println();
        System.out.println("=== Check balance via a Command that captures its own result ===");
        CheckBalanceCommand checkBalance = new CheckBalanceCommand(account);
        service.submitCommand(checkBalance);
        System.out.println("CheckBalanceCommand reports: " + checkBalance.getLastReadBalance());

        System.out.println();
        System.out.println("=== Deposit, then attempt an overdraft ===");
        service.submitCommand(new DepositCommand(account, new BigDecimal("20.00")));
        System.out.println("Balance after depositing 20.00: " + account.getBalance());
        try {
            service.submitCommand(new WithdrawCommand(account, new BigDecimal("1000.00")));
        } catch (InsufficientFundsException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Eject card, attempt another transaction ===");
        session.ejectCard();
        System.out.println("Session status: " + session.getStatus());
        try {
            service.submitCommand(new DepositCommand(account, new BigDecimal("5.00")));
        } catch (IllegalStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }
    }
}
