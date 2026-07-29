package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;

// The result of applying a rate to one shift: hours worked AND the resulting pay, not just
// pay alone. PayrollService needs hoursWorked to build ShiftPayBreakdown for the caller — if
// ShiftPayCalculator only returned the pay figure, PayrollService would have to recompute
// hoursWorked itself from the shift's Duration fields, duplicating the exact conversion
// logic StandardShiftPayCalculator already does.
public record CalculatedShiftPay(BigDecimal hoursWorked, BigDecimal pay) {}
