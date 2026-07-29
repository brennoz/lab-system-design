package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;

// One line per shift in the final payslip-shaped output: what rate applied, how many hours,
// what it paid. Returning this (not just a single opaque total) is what makes the
// calculation independently verifiable per shift, and is what an actual payroll system would
// need to show a colleague their payslip.
public record ShiftPayBreakdown(String shiftId, BigDecimal hourlyRate, BigDecimal hoursWorked, BigDecimal pay) {}
