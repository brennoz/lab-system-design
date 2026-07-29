package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;

// Pattern: Strategy — the pay-rate exercise explicitly names "different rates for overtime
// hours" as a coming Phase 2 requirement. Putting the rate-application algorithm behind this
// interface means Phase 2 adds a new implementation (splitting worked hours at a threshold,
// applying a second rate to the excess) and swaps it into PayrollService's constructor —
// zero changes to PayrollService, Employee, Shift, or PayRateRepository.
//
// Scope of that "zero changes" guarantee: it holds for a PER-SHIFT overtime threshold (e.g.
// hours beyond 8 within a single shift), since calculate() only ever sees one Shift at a
// time. A WEEKLY-CUMULATIVE threshold (e.g. hours 41+ across all of an employee's shifts
// that week) is a different shape of requirement — calculate() would need to know how many
// hours this employee has already worked so far this week, which this signature doesn't
// carry. That would mean widening this interface (e.g. passing a running total, or the full
// shift list) and touching PayrollService's call site — worth saying explicitly rather than
// letting the interface's simplicity imply a bigger promise than it can keep.
//
// Rejected alternative: Decorator (as used elsewhere in this project for stackable shopping
// discounts). Decorator suits independent ADDITIVE charges layered on a fixed base. Overtime
// isn't additive — it changes how the EXISTING hours are priced past a threshold within the
// same calculation, not a bonus stacked on top. Forcing it into Decorator would mean the
// decorator re-deriving the base hours anyway, defeating the pattern's purpose.
//
// What NOT to do: don't add an "isOvertime" boolean parameter to this method as a shortcut
// instead of a second implementation — that's the inline-if approach the Strategy pattern
// exists specifically to avoid, and it would force every future caller to know about
// overtime instead of just picking a different calculator.
public interface ShiftPayCalculator {
    CalculatedShiftPay calculate(Shift shift, BigDecimal hourlyRate);
}
