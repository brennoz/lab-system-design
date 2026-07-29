package com.interview.ood.q048_skill_based_pay.solution;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolutionTest {

    private final PayRateRepository rates = PayRateRepository.standardRates();
    private final ShiftPayCalculator calculator = new StandardShiftPayCalculator();
    private final PayrollService payrollService = new PayrollService(rates, calculator);

    // What this verifies: contract rate + skill rate are actually ADDED (differential pay),
    // and worked hours are correctly derived as total - break, not total alone. A version
    // that forgot to subtract the break would produce 8h x 14.50 = 116.00, not 108.75 — this
    // test catches that specific bug shape.
    @Test
    void singleShiftSingleSkillCalculatesCorrectPay() {
        Employee alice = new Employee("E1", ContractType.PERMANENT, List.of(
                new Shift("S1", Skill.BAKERY, Duration.ofHours(8), Duration.ofMinutes(30))
        ));

        WeeklyPayResult result = payrollService.calculateWeeklyPay(alice);

        // Permanent (12.50) + Bakery (2.00) = 14.50/h, worked 7.5h -> 108.75
        assertEquals(0, result.totalPay().compareTo(new BigDecimal("108.75")));
    }

    // What this verifies: the total is a genuine SUM of independently-rated shifts, not a
    // single flat rate applied to combined hours. A bug that averaged the two skill rates
    // instead of pricing each shift separately would still produce a plausible-looking
    // number here, but not 194.00.
    @Test
    void multiShiftDifferentSkillsSumsCorrectly() {
        Employee alice = new Employee("E1", ContractType.PERMANENT, List.of(
                new Shift("S1", Skill.BAKERY, Duration.ofHours(8), Duration.ofMinutes(30)),
                new Shift("S2", Skill.SHIFT_LEADER, Duration.ofHours(6), Duration.ofMinutes(30))
        ));

        WeeklyPayResult result = payrollService.calculateWeeklyPay(alice);

        // 108.75 (bakery shift) + 85.25 (shift leader shift) = 194.00
        assertEquals(0, result.totalPay().compareTo(new BigDecimal("194.00")));
    }

    // What this verifies: contract rate is read per-employee from PayRateRepository, not a
    // hardcoded constant somewhere in the calculation path — same shift, same skill, two
    // different contract types must produce two different totals.
    @Test
    void sameSkillDifferentContractTypesProduceDifferentTotals() {
        Shift shift = new Shift("S1", Skill.CHECKOUT_CASHIER, Duration.ofHours(7), Duration.ofMinutes(30));

        Employee permanentEmployee = new Employee("E1", ContractType.PERMANENT, List.of(shift));
        Employee agencyEmployee = new Employee("E2", ContractType.AGENCY, List.of(shift));

        BigDecimal permanentTotal = payrollService.calculateWeeklyPay(permanentEmployee).totalPay();
        BigDecimal agencyTotal = payrollService.calculateWeeklyPay(agencyEmployee).totalPay();

        // Permanent: (12.50+1.20=13.70) * 6.5h = 89.05
        // Agency:    (13.00+1.20=14.20) * 6.5h = 92.30
        assertEquals(0, permanentTotal.compareTo(new BigDecimal("89.05")));
        assertEquals(0, agencyTotal.compareTo(new BigDecimal("92.30")));
        assertNotEquals(0, permanentTotal.compareTo(agencyTotal));
    }

    // What this verifies: the breakdown isn't just decorative — each entry's shiftId and
    // hourlyRate are traceable back to the actual shift, and the entries genuinely sum to
    // the reported total rather than the total being computed by some separate, potentially
    // divergent code path.
    @Test
    void breakdownAccuracyMatchesShiftsAndSumsToTotal() {
        Employee alice = new Employee("E1", ContractType.PERMANENT, List.of(
                new Shift("S1", Skill.BAKERY, Duration.ofHours(8), Duration.ofMinutes(30)),
                new Shift("S2", Skill.SHIFT_LEADER, Duration.ofHours(6), Duration.ofMinutes(30))
        ));

        WeeklyPayResult result = payrollService.calculateWeeklyPay(alice);

        assertEquals(2, result.breakdown().size());
        assertEquals("S1", result.breakdown().get(0).shiftId());
        assertEquals(0, result.breakdown().get(0).hourlyRate().compareTo(new BigDecimal("14.50")));
        assertEquals(0, result.breakdown().get(0).hoursWorked().compareTo(new BigDecimal("7.5")));

        BigDecimal summedFromBreakdown = result.breakdown().stream()
                .map(ShiftPayBreakdown::pay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, summedFromBreakdown.compareTo(result.totalPay()));
    }

    // What this verifies: a shift where the break consumes the entire (or more than the
    // entire) total duration is rejected as invalid input, not silently treated as a
    // zero-or-negative-hour shift that happens to compute to £0.00 or a negative pay figure.
    @Test
    void shiftWithBreakEqualToOrLongerThanTotalDurationThrows() {
        Employee alice = new Employee("E1", ContractType.PERMANENT, List.of(
                new Shift("S1", Skill.BAKERY, Duration.ofHours(1), Duration.ofHours(1))
        ));

        assertThrows(IllegalArgumentException.class, () -> payrollService.calculateWeeklyPay(alice));
    }

    // What this verifies: the batch overload isn't just a loop that happens to work for one
    // employee — it returns exactly one result per input employee, in the same order, so a
    // caller can zip the input list against the output list by index.
    @Test
    void batchCalculationReturnsOneResultPerEmployeeInOrder() {
        Employee alice = new Employee("E1", ContractType.PERMANENT, List.of(
                new Shift("S1", Skill.BAKERY, Duration.ofHours(8), Duration.ofMinutes(30))
        ));
        Employee bob = new Employee("E2", ContractType.AGENCY, List.of(
                new Shift("S2", Skill.CHECKOUT_CASHIER, Duration.ofHours(7), Duration.ofMinutes(30))
        ));

        List<WeeklyPayResult> results = payrollService.calculateWeeklyPay(List.of(alice, bob));

        assertEquals(2, results.size());
        assertEquals("E1", results.get(0).employeeId());
        assertEquals("E2", results.get(1).employeeId());
        assertEquals(0, results.get(1).totalPay().compareTo(new BigDecimal("92.30")));
    }

    // Regression test for a real bug found and fixed during this implementation: an earlier
    // version of StandardShiftPayCalculator rounded hoursWorked to 4dp BEFORE multiplying by
    // the rate (double-rounding), which produced £2.97 for this exact scenario. The correct,
    // single-rounded figure is £2.98 (17 minutes x £10.50/h = £2.975 -> rounds to £2.98).
    // Uses a custom rate table (via PayRateRepository's constructor injection, not the
    // standard Tesco rates) specifically to reach an odd-minute worked duration that exposes
    // the bug — none of Tesco's real published rates happen to trigger it on a whole number
    // of minutes, which is exactly why this bug wasn't caught by the other tests above.
    @Test
    void oddMinuteDurationRoundsToCorrectPennyNotDoubleRounded() {
        Map<Skill, BigDecimal> skillRates = new EnumMap<>(Skill.class);
        for (Skill skill : Skill.values()) {
            skillRates.put(skill, BigDecimal.ZERO);
        }
        Map<ContractType, BigDecimal> contractRates = new EnumMap<>(ContractType.class);
        for (ContractType type : ContractType.values()) {
            contractRates.put(type, BigDecimal.ZERO);
        }
        contractRates.put(ContractType.PERMANENT, new BigDecimal("10.50"));

        PayRateRepository customRates = new PayRateRepository(skillRates, contractRates);
        PayrollService customPayrollService = new PayrollService(customRates, calculator);

        Employee employee = new Employee("E1", ContractType.PERMANENT, List.of(
                new Shift("S1", Skill.BAKERY, Duration.ofMinutes(17), Duration.ZERO)
        ));

        WeeklyPayResult result = customPayrollService.calculateWeeklyPay(employee);

        assertEquals(0, result.totalPay().compareTo(new BigDecimal("2.98")));
    }
}
