package com.interview.ood.q044_atm_system.solution;

import java.math.BigDecimal;

// Design decision: a dedicated exception, not IllegalArgumentException. Overdraft is a
// named business rule an ATM interviewer expects called out explicitly, not folded into a
// generic "bad argument" — same reasoning as q030_library_system's
// BorrowLimitExceededException existing instead of a bare IllegalStateException.
//
// What NOT to do: don't make this a checked exception. A caller (AtmService, AtmMain) isn't
// expected to recover from an overdraft attempt mid-flow the way it might retry a checked
// I/O failure — it's a business-rule rejection best handled at a boundary (present the
// message to the user), which is exactly what unchecked RuntimeException-based exceptions
// are for in this codebase (see the rest of q030/q043's exception hierarchy — none are
// checked).
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String accountNumber, BigDecimal requested, BigDecimal available) {
        super("Account " + accountNumber + ": requested " + requested + " but only " + available + " available");
    }
}
