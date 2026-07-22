package com.interview.ood.q044_atm_system.solution;

// Design decision (Pattern: Command) — a transaction request (withdraw/deposit/check
// balance) is an object with a single execute() method, not a method call directly against
// Account from AtmService. This is the first Command pattern used in this project's OOD
// questions (q030/q042/q043 all used Specification/State/Strategy) — worth naming
// explicitly since it's easy to mistake for Strategy: Strategy picks *how* to do one fixed
// kind of thing (see q043's SchedulingStrategy), Command represents *a request to do a
// specific thing*, decoupled from when or by whom it's executed.
//
// Alternative considered: AtmService exposing withdraw(Account, BigDecimal),
// deposit(Account, BigDecimal), checkBalance(Account) methods directly, no Command objects
// at all. Rejected because it doesn't scale to the features a real ATM interview question
// usually asks for next — a transaction log, an undo/reversal, or queuing a transaction for
// batch processing all become natural once a transaction is a first-class object you can
// store, log, or replay, rather than a method call that's already finished by the time
// you'd want to log it.
//
// What NOT to do: don't give AtmService any knowledge of which concrete command it's
// running (an instanceof check to special-case WithdrawCommand, for example) — the entire
// point is that AtmService only ever calls execute() and never needs to change when a new
// command type is added.
public interface AtmCommand {
    void execute();
}
