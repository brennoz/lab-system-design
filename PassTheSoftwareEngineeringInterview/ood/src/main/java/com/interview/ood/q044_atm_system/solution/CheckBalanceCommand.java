package com.interview.ood.q044_atm_system.solution;

import java.math.BigDecimal;

// Design decision: unlike WithdrawCommand/DepositCommand, this command captures its own
// result (getLastReadBalance()) after execute() runs. Classic Command pattern is usually
// pure void — the deviation here is deliberate: a balance check has nothing to mutate, so
// its entire purpose IS the result, and the caller (AtmService, AtmMain) needs a way to
// retrieve it without AtmService special-casing "this one command type returns something."
//
// What NOT to do: don't call getLastReadBalance() before execute() has run — it returns
// null until then. An alternative that avoids this pitfall entirely would be having
// execute() return BigDecimal directly instead of implementing the shared void-returning
// AtmCommand interface, but that would mean CheckBalanceCommand couldn't be submitted
// through AtmService.submitCommand() alongside the other two — the uniform interface is
// worth the getter's null-before-execute() caveat.
public class CheckBalanceCommand implements AtmCommand {

    private final Account account;
    private BigDecimal lastReadBalance;

    public CheckBalanceCommand(Account account) {
        this.account = account;
    }

    @Override
    public void execute() {
        lastReadBalance = account.getBalance();
    }

    public BigDecimal getLastReadBalance() {
        return lastReadBalance;
    }
}
