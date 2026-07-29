package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.util.List;

// The final payslip-shaped result for one employee's week: the reported total plus the full
// per-shift breakdown it was built from. Compact constructor defensively copies breakdown
// (List.copyOf) for the same reason Employee.shifts does — without it, a caller holding a
// reference to the list passed into PayrollService could mutate it after construction and
// silently corrupt an otherwise-immutable result.
public record WeeklyPayResult(String employeeId, BigDecimal totalPay, List<ShiftPayBreakdown> breakdown) {
    public WeeklyPayResult {
        breakdown = List.copyOf(breakdown);
    }
}
