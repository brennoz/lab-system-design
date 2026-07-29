# q048 Skill-Based Pay Calculation System (Tesco interview exercise)

**Date:** 2026-07-29
**Project:** PassTheSoftwareEngineeringInterview
**Status:** Approved

## Context

Real take-home OOD exercise from a live Tesco interview process (screenshot provided by
user, transcribed verbatim into `question.md`). No starter code/interfaces were given — full
design freedom on classes and API shape. Precedent for handling a real (not generated)
take-home in this repo: q042 (`library_search`, "Real NatWest 3rd-round take-home").

Per user's explicit choices: no HTML teaching lesson for this one (time-sensitive, real
interview) — code + tests only, following this project's standard OOD conventions (SOLID,
named patterns, `BigDecimal` for money, JUnit 5 + Mockito, no AssertJ).

## Problem (from the exercise)

Tesco wants a skill-based pay system: employees get their contracted hourly rate PLUS a
per-skill differential rate for each shift, based on the skill they worked that shift.
Phase 1 only combines contract pay + skill pay (no overtime yet). A **named future
enhancement** — different rates for overtime hours — is explicitly flagged as coming next.

**Input**: a list of employees, each with an id, a contract type, and a list of shifts
worked that week. Each shift has an id, a skill, a total duration, and a break duration.

**Rate tables** (fixed for this phase):

| Skill | Rate/hour |
|---|---|
| Bakery | £2.00 |
| Checkout Cashier | £1.20 |
| Customer Service | £1.30 |
| Shift Leader | £3.00 |
| Security | £1.00 |
| Cleaning | £1.00 |
| Delivery Driver | £2.00 |

| Contract type | Rate/hour |
|---|---|
| Permanent | £12.50 |
| Agency | £13.00 |
| Zero-hours | £13.00 |

## Domain model

Thin records (no embedded behavior — matches this project's standing "prod-leaning OOD"
convention from q033 onward: domain objects are data containers, service classes own logic):

- `Employee(String id, ContractType contractType, List<Shift> shifts)`
- `Shift(String id, Skill skill, Duration totalDuration, Duration breakDuration)`
- `enum ContractType { PERMANENT, AGENCY, ZERO_HOURS }`
- `enum Skill { BAKERY, CHECKOUT_CASHIER, CUSTOMER_SERVICE, SHIFT_LEADER, SECURITY, CLEANING, DELIVERY_DRIVER }`

`java.time.Duration` for all time values (not raw numbers or `BigDecimal` hours) — removes
any ambiguity about whether a raw number means minutes or hours; conversion to decimal hours
happens once, at the point a rate is actually applied.

## Rate lookup — `PayRateRepository`

Two `EnumMap`s (`Skill -> BigDecimal`, `ContractType -> BigDecimal`), built via a
`PayRateRepository.standardRates()` factory matching the tables above exactly.
`BigDecimal` throughout, never `double` — this project's standing rule for anything
money-shaped, doubly relevant here since this is literally a payroll calculation.

## Core design decision — Strategy Pattern for pay-rate application

The spec explicitly names "different rates for overtime hours" as a coming requirement —
that decides the architecture, not just today's flat calculation.

**Chosen: Strategy.** `ShiftPayCalculator` interface: `BigDecimal calculate(Shift shift,
BigDecimal hourlyRate)`. Phase 1's only implementation, `StandardShiftPayCalculator`, computes
`workedHours × hourlyRate` (worked hours = total duration − break duration, no tiers).
`PayrollService` receives a `ShiftPayCalculator` via constructor injection. When overtime
ships, a new `OvertimePayShiftCalculator` implements the same interface (splits worked hours
at a threshold, applies a second rate to the excess) and swaps in — zero changes to
`PayrollService`, `Employee`, `Shift`, or `PayRateRepository`. Mirrors this repo's existing
`SchedulingStrategy` (q043).

**Rejected: Decorator** (the pattern q046's shopping-cart exercise used for stackable
offers). Decorator suits independent *additive* charges layered on a fixed base. Overtime
isn't additive — it changes how the *existing* hours are priced past a threshold within the
same calculation, not a bonus stacked on top. Forcing it into Decorator would mean the
decorator re-deriving the base hours anyway, defeating the pattern's purpose.

**Rejected: no pattern, inline `if` branch added later.** Fastest today, but this is
precisely what the spec is testing against — overtime is a named, upcoming requirement, not
a hypothetical, so designing for it now via Strategy is reading the brief, not speculative
over-engineering.

## Orchestration and output

`PayrollService(PayRateRepository rates, ShiftPayCalculator calculator)`.

- `WeeklyPayResult calculateWeeklyPay(Employee employee)` — for each shift: combined hourly
  rate = `rates.contractRate(employee.contractType()) + rates.skillRate(shift.skill())`;
  shift pay = `calculator.calculate(shift, combinedRate)`. Returns both the total and a
  per-shift breakdown (per user's explicit choice — a payroll system needs to show a
  colleague their payslip, and it makes the calculation independently verifiable per shift,
  not just as one opaque total).
- `List<WeeklyPayResult> calculateWeeklyPay(List<Employee> employees)` — batch convenience.

Result types (thin records):

- `ShiftPayBreakdown(String shiftId, BigDecimal hourlyRate, BigDecimal hoursWorked, BigDecimal pay)`
- `WeeklyPayResult(String employeeId, BigDecimal totalPay, List<ShiftPayBreakdown> breakdown)`

**Validation**: `breakDuration >= totalDuration` is invalid (zero or negative worked time).
Validated in `PayrollService`, not the `Shift` record — matches how `SocialGraph.follow()`
(not the `Follow` record) validates self-follow elsewhere in this repo. Throws
`IllegalArgumentException`.

## File layout

```
ood/src/main/java/com/interview/ood/q048_skill_based_pay/
  question.md                  (Tesco spec, transcribed)
  solution/
    Employee.java, Shift.java, ContractType.java, Skill.java
    PayRateRepository.java
    ShiftPayCalculator.java, StandardShiftPayCalculator.java
    ShiftPayBreakdown.java, WeeklyPayResult.java
    PayrollService.java
    PayrollMain.java          (runnable demo — matches this project's q044+ Main-class rule)
  answer/
    .gitkeep

ood/src/test/java/com/interview/ood/q048_skill_based_pay/
  solution/
    SolutionTest.java
  answer/
    .gitkeep
```

No HTML lesson for this question (user's explicit choice — real, time-sensitive interview
exercise; code + tests only).

## Test plan (`SolutionTest.java`)

1. Single shift, single skill — correct combined rate applied, correct hours computed from
   `totalDuration - breakDuration`.
2. Multiple shifts across different skills — total is the correct sum of per-shift pay, not
   an average or a flat multiply.
3. Same skill/hours, different contract types (Permanent vs Agency) — produces different
   totals, proving contract rate is actually applied per-employee, not hardcoded.
4. Breakdown accuracy — `WeeklyPayResult.breakdown()` contains one `ShiftPayBreakdown` per
   shift with the correct `shiftId`/`hourlyRate`/`hoursWorked`/`pay`, and they sum to the
   reported total.
5. Invalid shift (`breakDuration >= totalDuration`) throws `IllegalArgumentException`.
6. Batch calculation (`List<Employee>`) — returns one `WeeklyPayResult` per employee, in
   correspondence with the input list.

~6 tests, grouped in one `SolutionTest.java` (matches this project's precedent for
multi-class OOD solutions — see q044's `AtmMain`/`SolutionTest` split).

## Out of scope

- Overtime calculation itself (explicitly future work per the spec) — only the Strategy seam
  for it is built now.
- No HTML lesson (user's choice for this question).
- No persistence layer beyond in-memory rate maps — this is a pure calculation exercise, not
  a system-design one.
