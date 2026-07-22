package com.interview.ood.q044_atm_system.solution;

import java.math.BigDecimal;

public class DepositCommand implements AtmCommand {

    private final Account account;
    private final BigDecimal amount;

    public DepositCommand(Account account, BigDecimal amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void execute() {
        account.deposit(amount);
    }
}
