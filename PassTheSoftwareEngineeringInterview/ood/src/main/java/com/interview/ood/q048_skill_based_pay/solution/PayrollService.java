package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Orchestrator: for each shift, combines the employee's contract rate with that shift's
// skill rate, delegates the actual pay-rate application to the injected ShiftPayCalculator
// (Strategy), and assembles the per-shift breakdown + running total. PayrollService itself
// never does hourly-rate arithmetic beyond the simple contract+skill addition — that keeps
// a PER-SHIFT Phase-2 overtime swap (a new ShiftPayCalculator implementation) from requiring
// any change here. A WEEKLY-CUMULATIVE overtime threshold is a different shape of
// requirement and would need this loop to change too — see ShiftPayCalculator's comment for
// the full reasoning.
public class PayrollService {

    private final PayRateRepository rates;
    private final ShiftPayCalculator calculator;

    public PayrollService(PayRateRepository rates, ShiftPayCalculator calculator) {
        this.rates = rates;
        this.calculator = calculator;
    }

    public WeeklyPayResult calculateWeeklyPay(Employee employee) {
        BigDecimal contractRate = rates.contractRate(employee.contractType());
        List<ShiftPayBreakdown> breakdown = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Shift shift : employee.shifts()) {
            validateShift(shift);
            BigDecimal hourlyRate = contractRate.add(rates.skillRate(shift.skill()));
            CalculatedShiftPay calculated = calculator.calculate(shift, hourlyRate);
            breakdown.add(new ShiftPayBreakdown(shift.id(), hourlyRate,
                    calculated.hoursWorked(), calculated.pay()));
            total = total.add(calculated.pay());
        }

        return new WeeklyPayResult(employee.id(), total, breakdown);
    }

    public List<WeeklyPayResult> calculateWeeklyPay(List<Employee> employees) {
        return employees.stream().map(this::calculateWeeklyPay).toList();
    }

    // Validated here, not in the Shift record itself — same split this project uses
    // elsewhere (e.g. SocialGraph.follow() validates self-follow, not the Follow record).
    // A shift where break duration is >= total duration implies zero or negative worked
    // time, which is invalid input data, not a legitimate zero-pay shift.
    private void validateShift(Shift shift) {
        if (shift.totalDuration().compareTo(shift.breakDuration()) <= 0) {
            throw new IllegalArgumentException(
                    "Shift " + shift.id() + ": break duration must be less than total duration");
        }
    }
}
