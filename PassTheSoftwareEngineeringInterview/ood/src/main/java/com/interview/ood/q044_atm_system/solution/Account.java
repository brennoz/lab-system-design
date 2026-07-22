package com.interview.ood.q044_atm_system.solution;

import java.math.BigDecimal;

// Design decision: Account, not WithdrawCommand or AtmService, is the one place that
// enforces "no overdraft." A Command's job (see AtmCommand) is to encapsulate a *request*,
// not to own the business rule about whether it's allowed — the same separation
// q042_library_search's Specification classes draw between "what criterion" and "who
// evaluates the catalogue."
//
// What NOT to do: don't use double or float for balance — this project's standing fintech
// rule (see CLAUDE.md's Sport Betting/Banking sections): floating-point arithmetic loses
// cents at scale and fails regulatory precision requirements. BigDecimal, always, for money.
public class Account {

    private final String accountNumber;
    private BigDecimal balance;

    public Account(String accountNumber, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException(accountNumber, amount, balance);
        }
        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
