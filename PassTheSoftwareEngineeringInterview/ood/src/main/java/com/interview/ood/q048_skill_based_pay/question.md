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

That "no other changes needed" guarantee holds for a **per-shift** overtime threshold (e.g.
hours beyond 8 within a single shift), since `ShiftPayCalculator.calculate()` only ever sees
one shift at a time. A **weekly-cumulative** threshold (e.g. hours 41+ across all shifts an
employee worked that week) is a different shape of requirement — it would need the
calculator to know how many hours the employee has already worked so far that week, which
this signature doesn't carry, so it would require widening the interface and touching
`PayrollService`'s call site too. Worth being explicit about this distinction if asked in
interview — the design doesn't assume which shape overtime will take, but the "zero changes"
claim specifically covers the per-shift case.

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
