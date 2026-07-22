package com.interview.ood.q044_atm_system.solution;

import java.math.BigDecimal;

// What NOT to do: don't validate the amount or check the balance here. Overdraft rejection
// belongs to Account (see Account.withdraw()) — this class only encapsulates *the request*
// (which account, how much), not the business rule about whether it succeeds. Duplicating
// the balance check here would create two places that could disagree about what's allowed.
public class WithdrawCommand implements AtmCommand {

    private final Account account;
    private final BigDecimal amount;

    public WithdrawCommand(Account account, BigDecimal amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void execute() {
        account.withdraw(amount);
    }
}
