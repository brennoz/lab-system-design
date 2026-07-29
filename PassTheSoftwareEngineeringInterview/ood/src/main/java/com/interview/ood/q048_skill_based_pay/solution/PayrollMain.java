package com.interview.ood.q048_skill_based_pay.solution;

import java.time.Duration;
import java.util.List;

// Runnable demo — not a test. Builds two colleagues across three shifts and three different
// skills/contract types, prints each one's payslip-shaped breakdown plus the batch call.
// Precedent: q044's AtmMain.java.
public class PayrollMain {

    public static void main(String[] args) {
        PayRateRepository rates = PayRateRepository.standardRates();
        ShiftPayCalculator calculator = new StandardShiftPayCalculator();
        PayrollService payrollService = new PayrollService(rates, calculator);

        Employee alice = new Employee("E1", ContractType.PERMANENT, List.of(
                new Shift("S1", Skill.BAKERY, Duration.ofHours(8), Duration.ofMinutes(30)),
                new Shift("S2", Skill.SHIFT_LEADER, Duration.ofHours(6), Duration.ofMinutes(30))
        ));

        Employee bob = new Employee("E2", ContractType.AGENCY, List.of(
                new Shift("S3", Skill.CHECKOUT_CASHIER, Duration.ofHours(7), Duration.ofMinutes(30))
        ));

        System.out.println("=== Weekly pay for alice (Permanent, Bakery + Shift Leader) ===");
        printResult(payrollService.calculateWeeklyPay(alice));

        System.out.println();
        System.out.println("=== Weekly pay for bob (Agency, Checkout Cashier) ===");
        printResult(payrollService.calculateWeeklyPay(bob));

        System.out.println();
        System.out.println("=== Batch calculation for alice + bob ===");
        List<WeeklyPayResult> batch = payrollService.calculateWeeklyPay(List.of(alice, bob));
        batch.forEach(PayrollMain::printResult);
    }

    private static void printResult(WeeklyPayResult result) {
        System.out.println("Employee " + result.employeeId() + " total pay: £" + result.totalPay());
        for (ShiftPayBreakdown b : result.breakdown()) {
            System.out.println("  shift " + b.shiftId() + ": " + b.hoursWorked() + "h @ £"
                    + b.hourlyRate() + "/h = £" + b.pay());
        }
    }
}
