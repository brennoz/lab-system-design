package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.util.List;

public record WeeklyPayResult(String employeeId, BigDecimal totalPay, List<ShiftPayBreakdown> breakdown) {
    public WeeklyPayResult {
        breakdown = List.copyOf(breakdown);
    }
}
