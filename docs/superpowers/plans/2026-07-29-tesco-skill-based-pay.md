# q048 Tesco Skill-Based Pay Calculation System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete, tested OOD solution for a real Tesco take-home interview exercise (skill-based weekly pay calculation) under `ood/src/.../q048_skill_based_pay/`, following this project's standard OOD conventions.

**Architecture:** Thin domain records (`Employee`, `Shift`) + two enums (`ContractType`, `Skill`) + a Map-based rate repository (`PayRateRepository`) + a Strategy-pattern pay-rate calculator (`ShiftPayCalculator`, with `StandardShiftPayCalculator` as the Phase 1 implementation, leaving room for a future `OvertimePayShiftCalculator`) + an orchestrating `PayrollService` that returns a per-employee total plus a per-shift breakdown. No HTML lesson for this question (user's explicit choice — real, time-sensitive interview exercise).

**Tech Stack:** Java 21, `java.time.Duration` for shift times, `BigDecimal` for all money, JUnit 5 only (no Mockito needed — no external collaborators to mock, no AssertJ per project-wide rule).

---

## Reference: spec

Full design at `docs/superpowers/specs/2026-07-29-tesco-skill-based-pay-design.md`.

## Reference: existing conventions being followed

- Folder layout precedent: `ood/src/main/java/com/interview/ood/q044_atm_system/{solution,answer}/` and mirrored `src/test` folders.
- `BigDecimal` comparisons in tests: `assertEquals(0, actual.compareTo(expected))`, never `assertEquals(expected, actual)` directly (`BigDecimal.equals()` is scale-sensitive).
- New tests: JUnit 5 (`org.junit.jupiter.api.Assertions`) only, no AssertJ.
- Runnable `{Entity}Main.java` demo precedent: `q044_atm_system/solution/AtmMain.java`.

---

### Task 1: question.md

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/question.md`

- [ ] **Step 1: Write the file**

```markdown
# q048 — Tesco: Skill-Based Pay Calculation System

**Category:** OOD · **Difficulty:** Medium · **Source:** Real Tesco interview take-home exercise

---

## Problem (verbatim from the exercise)

Tesco wants to implement a skill-based pay system to reward employees for their specialised
skills. This system will provide differential pay in addition to their regular contracted
pay. In the first phase, Tesco is only paying based on the contract and the skill they
worked on. Write a Skill-based pay calculation system to calculate the total pay for a week
for the given colleagues.

As a future enhancement, Tesco plans to introduce different rates for overtime hours.

### Input

You will be provided with a list of employees and the shifts they worked during the week,
with the following information:

- Employee id
- List of shifts worked
- Contract Type

Each shift will have the following information:

- Shift id
- Skill
- Total Duration
- Break duration

### Skill Pay

| Skill | Pay Rate |
|---|---|
| Bakery | £2/hour |
| Checkout Cashier | £1.2/hour |
| Customer Service | £1.3/hour |
| Shift Leader | £3/hour |
| Security | £1/hour |
| Cleaning | £1/hour |
| Delivery Driver | £2/hour |

### Contract Pay

| Contract type | Pay Rate |
|---|---|
| Permanent | £12.5/hour |
| Agency | £13/hour |
| Zero-hours | £13/hour |

---

## Design notes

No starter code/interfaces were provided — full design freedom. Solved following this
project's standard OOD conventions (SOLID, named patterns, `BigDecimal` for money,
`java.time.Duration` for shift times, JUnit 5).

The spec explicitly names "different rates for overtime hours" as a coming enhancement — the
design uses **Strategy Pattern** (`ShiftPayCalculator`) for the pay-rate application step
specifically so that requirement can be added later as a new implementation, without
changing `PayrollService`, `Employee`, `Shift`, or the rate repository.

---

## Folder layout

```
q048_skill_based_pay/
├── question.md
├── solution/   ← full working implementation, one class per file, includes PayrollMain.java
└── answer/     ← empty (.gitkeep)
```

No HTML teaching lesson for this question — real, time-sensitive interview exercise, code +
tests only.
```

- [ ] **Step 2: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q048_skill_based_pay/question.md
git commit -m "docs(q048): add Tesco skill-based pay exercise spec"
```

---

### Task 2: Domain model — Employee, Shift, ContractType, Skill

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ContractType.java`
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Skill.java`
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Employee.java`
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Shift.java`

- [ ] **Step 1: Write `ContractType.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

// The three contract types Tesco pays a different base hourly rate for. A closed,
// fixed set — an enum gives compile-time safety over a free-text "contractType" string
// and lets PayRateRepository use an EnumMap for O(1), always-dense rate lookup.
public enum ContractType {
    PERMANENT,
    AGENCY,
    ZERO_HOURS
}
```

- [ ] **Step 2: Write `Skill.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

// The skills a shift can be worked under, each carrying its own pay differential.
// Same reasoning as ContractType — fixed, closed set, enum over free-text string.
public enum Skill {
    BAKERY,
    CHECKOUT_CASHIER,
    CUSTOMER_SERVICE,
    SHIFT_LEADER,
    SECURITY,
    CLEANING,
    DELIVERY_DRIVER
}
```

- [ ] **Step 3: Write `Shift.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.time.Duration;

// Thin data record — no behavior. Duration (not a raw number or BigDecimal hours) removes
// any ambiguity about whether totalDuration/breakDuration mean minutes or hours; conversion
// to decimal hours happens once, in StandardShiftPayCalculator, at the point a rate is
// actually applied.
public record Shift(String id, Skill skill, Duration totalDuration, Duration breakDuration) {}
```

- [ ] **Step 4: Write `Employee.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.util.List;

// Thin data record — no pay-calculation logic here. PayrollService owns the calculation,
// same "domain objects are data containers, service classes own logic" split this project
// uses throughout (see SocialGraph/LikeService vs Follow/Like in q035).
public record Employee(String id, ContractType contractType, List<Shift> shifts) {
    public Employee {
        shifts = List.copyOf(shifts);
    }
}
```

- [ ] **Step 5: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ContractType.java \
        ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Skill.java \
        ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Employee.java \
        ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Shift.java
git commit -m "feat(q048): add domain model (Employee, Shift, ContractType, Skill)"
```

---

### Task 3: PayRateRepository

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayRateRepository.java`

- [ ] **Step 1: Write the file**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

// EnumMap, not HashMap: both key types (Skill, ContractType) are fixed, closed enums, so
// EnumMap gives a dense array-backed lookup with no hashing — the natural map type once the
// key space is an enum. Rates are injected via constructor (not hardcoded as enum constants)
// so a test can supply its own rate table without touching production defaults, and so a
// real system could later load this from configuration instead of the standardRates()
// factory below.
public class PayRateRepository {

    private final Map<Skill, BigDecimal> skillRates;
    private final Map<ContractType, BigDecimal> contractRates;

    public PayRateRepository(Map<Skill, BigDecimal> skillRates, Map<ContractType, BigDecimal> contractRates) {
        this.skillRates = new EnumMap<>(skillRates);
        this.contractRates = new EnumMap<>(contractRates);
    }

    // Builds the exact rate table from the Tesco exercise spec.
    public static PayRateRepository standardRates() {
        Map<Skill, BigDecimal> skills = new EnumMap<>(Skill.class);
        skills.put(Skill.BAKERY, new BigDecimal("2.00"));
        skills.put(Skill.CHECKOUT_CASHIER, new BigDecimal("1.20"));
        skills.put(Skill.CUSTOMER_SERVICE, new BigDecimal("1.30"));
        skills.put(Skill.SHIFT_LEADER, new BigDecimal("3.00"));
        skills.put(Skill.SECURITY, new BigDecimal("1.00"));
        skills.put(Skill.CLEANING, new BigDecimal("1.00"));
        skills.put(Skill.DELIVERY_DRIVER, new BigDecimal("2.00"));

        Map<ContractType, BigDecimal> contracts = new EnumMap<>(ContractType.class);
        contracts.put(ContractType.PERMANENT, new BigDecimal("12.50"));
        contracts.put(ContractType.AGENCY, new BigDecimal("13.00"));
        contracts.put(ContractType.ZERO_HOURS, new BigDecimal("13.00"));

        return new PayRateRepository(skills, contracts);
    }

    public BigDecimal skillRate(Skill skill) {
        BigDecimal rate = skillRates.get(skill);
        if (rate == null) {
            throw new IllegalStateException("No pay rate configured for skill: " + skill);
        }
        return rate;
    }

    public BigDecimal contractRate(ContractType contractType) {
        BigDecimal rate = contractRates.get(contractType);
        if (rate == null) {
            throw new IllegalStateException("No pay rate configured for contract type: " + contractType);
        }
        return rate;
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayRateRepository.java
git commit -m "feat(q048): add PayRateRepository with standard Tesco rate table"
```

---

### Task 4: ShiftPayCalculator (Strategy) — interface, result type, Phase 1 implementation

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/CalculatedShiftPay.java`
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ShiftPayCalculator.java`
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/StandardShiftPayCalculator.java`

- [ ] **Step 1: Write `CalculatedShiftPay.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;

// The result of applying a rate to one shift: hours worked AND the resulting pay, not just
// pay alone. PayrollService needs hoursWorked to build ShiftPayBreakdown for the caller — if
// ShiftPayCalculator only returned the pay figure, PayrollService would have to recompute
// hoursWorked itself from the shift's Duration fields, duplicating the exact conversion
// logic StandardShiftPayCalculator already does.
public record CalculatedShiftPay(BigDecimal hoursWorked, BigDecimal pay) {}
```

- [ ] **Step 2: Write `ShiftPayCalculator.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;

// Pattern: Strategy — the pay-rate exercise explicitly names "different rates for overtime
// hours" as a coming Phase 2 requirement. Putting the rate-application algorithm behind this
// interface means Phase 2 adds a new implementation (splitting worked hours at a threshold,
// applying a second rate to the excess) and swaps it into PayrollService's constructor —
// zero changes to PayrollService, Employee, Shift, or PayRateRepository.
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
```

- [ ] **Step 3: Write `StandardShiftPayCalculator.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

// Phase 1 implementation: flat hourlyRate x hoursWorked, no tiers. hoursWorked is derived
// from the shift's own Duration fields (total - break), converted to decimal hours only
// here, at the one place a rate is actually applied.
public class StandardShiftPayCalculator implements ShiftPayCalculator {

    // Extra scale on the intermediate hours-worked figure avoids compounding rounding error
    // before the final multiply; PAY_SCALE (2) matches real currency precision (pence) for
    // the number that's actually paid out.
    private static final int HOURS_SCALE = 4;
    private static final int PAY_SCALE = 2;

    @Override
    public CalculatedShiftPay calculate(Shift shift, BigDecimal hourlyRate) {
        Duration worked = shift.totalDuration().minus(shift.breakDuration());
        BigDecimal hoursWorked = BigDecimal.valueOf(worked.toMinutes())
                .divide(BigDecimal.valueOf(60), HOURS_SCALE, RoundingMode.HALF_UP);
        BigDecimal pay = hoursWorked.multiply(hourlyRate).setScale(PAY_SCALE, RoundingMode.HALF_UP);
        return new CalculatedShiftPay(hoursWorked, pay);
    }
}
```

- [ ] **Step 4: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/CalculatedShiftPay.java \
        ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ShiftPayCalculator.java \
        ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/StandardShiftPayCalculator.java
git commit -m "feat(q048): add ShiftPayCalculator Strategy interface and Phase 1 implementation"
```

---

### Task 5: Result types — ShiftPayBreakdown, WeeklyPayResult

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ShiftPayBreakdown.java`
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/WeeklyPayResult.java`

- [ ] **Step 1: Write `ShiftPayBreakdown.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;

// One line per shift in the final payslip-shaped output: what rate applied, how many hours,
// what it paid. Returning this (not just a single opaque total) is what makes the
// calculation independently verifiable per shift, and is what an actual payroll system would
// need to show a colleague their payslip.
public record ShiftPayBreakdown(String shiftId, BigDecimal hourlyRate, BigDecimal hoursWorked, BigDecimal pay) {}
```

- [ ] **Step 2: Write `WeeklyPayResult.java`**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.util.List;

public record WeeklyPayResult(String employeeId, BigDecimal totalPay, List<ShiftPayBreakdown> breakdown) {
    public WeeklyPayResult {
        breakdown = List.copyOf(breakdown);
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ShiftPayBreakdown.java \
        ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/WeeklyPayResult.java
git commit -m "feat(q048): add ShiftPayBreakdown and WeeklyPayResult result types"
```

---

### Task 6: PayrollService

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayrollService.java`

- [ ] **Step 1: Write the file**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Orchestrator: for each shift, combines the employee's contract rate with that shift's
// skill rate, delegates the actual pay-rate application to the injected ShiftPayCalculator
// (Strategy), and assembles the per-shift breakdown + running total. PayrollService itself
// never does hourly-rate arithmetic beyond the simple contract+skill addition — that keeps
// the Phase-2 overtime swap (a new ShiftPayCalculator implementation) from requiring any
// change here.
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
```

- [ ] **Step 2: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayrollService.java
git commit -m "feat(q048): add PayrollService orchestrating rate lookup and shift calculation"
```

---

### Task 7: PayrollMain demo

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayrollMain.java`

- [ ] **Step 1: Write the file**

```java
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
```

- [ ] **Step 2: Verify it compiles and the demo runs**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

`exec-maven-plugin` isn't configured in this project's `pom.xml`, so run the compiled class directly:

Run (from `/home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview`): `java -cp ood/target/classes com.interview.ood.q048_skill_based_pay.solution.PayrollMain`

Expected output (exact figures — verify these, not just that it runs):
```
=== Weekly pay for alice (Permanent, Bakery + Shift Leader) ===
Employee E1 total pay: £194.00
  shift S1: 7.5000h @ £14.50/h = £108.75
  shift S2: 5.5000h @ £15.50/h = £85.25

=== Weekly pay for bob (Agency, Checkout Cashier) ===
Employee E2 total pay: £92.30
  shift S3: 6.5000h @ £14.20/h = £92.30

=== Batch calculation for alice + bob ===
Employee E1 total pay: £194.00
  shift S1: 7.5000h @ £14.50/h = £108.75
  shift S2: 5.5000h @ £15.50/h = £85.25
Employee E2 total pay: £92.30
  shift S3: 6.5000h @ £14.20/h = £92.30
```

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayrollMain.java
git commit -m "feat(q048): add PayrollMain runnable demo"
```

---

### Task 8: SolutionTest.java — write and verify all 6 tests

**Files:**
- Create: `ood/src/test/java/com/interview/ood/q048_skill_based_pay/solution/SolutionTest.java`

- [ ] **Step 1: Write the test file**

```java
package com.interview.ood.q048_skill_based_pay.solution;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

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
}
```

- [ ] **Step 2: Run the tests**

Run: `mvn test -pl ood -Dtest=com.interview.ood.q048_skill_based_pay.solution.SolutionTest`
Expected: `Tests run: 6, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

If any test fails, the bug is in the Task 2-6 implementation (every method's exact behavior was already specified in those tasks) — fix the implementation file, not the test, then re-run this command until all 6 pass.

- [ ] **Step 3: Commit**

```bash
git add ood/src/test/java/com/interview/ood/q048_skill_based_pay/solution/SolutionTest.java
git commit -m "test(q048): add SolutionTest covering pay calculation, breakdown, and validation"
```

---

### Task 9: answer/ placeholders + folder mirroring

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q048_skill_based_pay/answer/.gitkeep`
- Create: `ood/src/test/java/com/interview/ood/q048_skill_based_pay/answer/.gitkeep`

- [ ] **Step 1: Create both empty placeholder files**

```bash
touch ood/src/main/java/com/interview/ood/q048_skill_based_pay/answer/.gitkeep
touch ood/src/test/java/com/interview/ood/q048_skill_based_pay/answer/.gitkeep
```

- [ ] **Step 2: Verify final folder layout matches the spec**

Run: `find ood/src/main/java/com/interview/ood/q048_skill_based_pay ood/src/test/java/com/interview/ood/q048_skill_based_pay -type f | sort`

Expected output:
```
ood/src/main/java/com/interview/ood/q048_skill_based_pay/answer/.gitkeep
ood/src/main/java/com/interview/ood/q048_skill_based_pay/question.md
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/CalculatedShiftPay.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ContractType.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Employee.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayRateRepository.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayrollMain.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/PayrollService.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Shift.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ShiftPayBreakdown.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/ShiftPayCalculator.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/Skill.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/StandardShiftPayCalculator.java
ood/src/main/java/com/interview/ood/q048_skill_based_pay/solution/WeeklyPayResult.java
ood/src/test/java/com/interview/ood/q048_skill_based_pay/answer/.gitkeep
ood/src/test/java/com/interview/ood/q048_skill_based_pay/solution/SolutionTest.java
```

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q048_skill_based_pay/answer/.gitkeep \
        ood/src/test/java/com/interview/ood/q048_skill_based_pay/answer/.gitkeep
git commit -m "chore(q048): add empty answer/ placeholders"
```

---

### Task 10: Full module regression check

**Files:** none (verification only)

- [ ] **Step 1: Run the full `ood` module test suite**

Run: `mvn test -pl ood`
Expected: `BUILD SUCCESS`, all tests pass including the new `SolutionTest` (6 tests) and every pre-existing `ood` test unaffected by this change.

- [ ] **Step 2: Confirm no stray references or leftover build artifacts**

Run: `grep -rl "q048" ood/src/main ood/src/test | sort`
Expected: only files under `q048_skill_based_pay/` appear — no other file in the module references q048.

This is the final task — no commit needed if both steps pass cleanly.
