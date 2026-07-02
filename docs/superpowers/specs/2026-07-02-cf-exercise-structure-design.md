# CompletableFuture Exercise Structure — Design Spec

**Date:** 2026-07-02
**Applies to:** All CompletableFuture exercises in `PassTheSoftwareEngineeringInterview/algorithms/`
**Supersedes:** Earlier structure that pre-generated models, interfaces, and test files in main package.

---

## Goal

Exercises where the learner builds everything from scratch — models, interfaces, MyAnswer — following TDD. Reference solution exists but is isolated in a `solution/` sub-package so it doesn't pollute the learner's workspace. This pattern applies to all future CF exercises.

---

## Directory Structure (per exercise)

```
algorithms/src/main/java/com/interview/algorithms/completable_future/q00N_<slug>/
  question.md              ← problem statement (co-located with Java, not in concepts/)
  MyAnswer.java            ← empty class shell; learner writes everything from scratch
  solution/
    <Model>.java           ← reference models (Java 21 records, heavily commented)
    <Service>.java         ← reference interfaces (heavily commented)
    Solution.java          ← reference implementation (heavily commented)

algorithms/src/test/java/com/interview/algorithms/completable_future/q00N_<slug>/
  (no pre-generated files — learner creates MyAnswerTest.java here via TDD)
  solution/
    SolutionTest.java      ← reference tests (heavily commented)
```

---

## Rules

### question.md
- Co-located with the Java source (inside `src/main/java/.../<exercise>/`)
- Contains: problem description, names of types to create, method signature to implement, CF methods to use, constraints, run command
- Does NOT contain pre-written code — just the brief
- One file per exercise

### MyAnswer.java
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

/**
 * Exercise N — <Name>
 * Read question.md before starting.
 * Create your own models, interfaces, and constructor from scratch.
 */
public class MyAnswer {
    // your implementation here
}
```
- No imports, no constructor, no method signature pre-generated
- Learner designs the API from the problem statement

### solution/ sub-package
- Package name: `com.interview.algorithms.completable_future.q001_subscription_chain.solution`
- Zero dependency on learner's types — fully self-contained
- All `.java` files have extensive comments explaining:
  - What the type is (record, interface, implementation class)
  - Why this design decision was made
  - Java/CF concept being demonstrated
  - Trade-offs vs alternatives
- Learner should only open `solution/` after attempting the exercise

### SolutionTest.java (in test/solution/)
- Package: `com.interview.algorithms.completable_future.q001_subscription_chain.solution`
- Tests `Solution.java`, not `MyAnswer.java`
- Heavily commented — explains why each test exists, what CF behaviour it verifies
- Serves as reference for the learner's own test design

---

## Comment Depth — solution/ files

Every non-trivial line should carry one of:
- `// WHY:` — design decision rationale
- `// CF:` — which CompletableFuture concept is being used and why
- `// Java:` — Java language feature note (records, sealed classes, local records, etc.)
- `// ALT:` — alternative approach and trade-off

Example:
```java
return subscriptionService.findById(subscriptionId)
    .thenApply(pricingService::applyDiscount)   // CF: sync transform — no IO, so thenApply not thenCompose
    .thenCompose(discounted ->                   // CF: async step — next call returns CF, so thenCompose flattens CF<CF<T>> to CF<T>
        accountService.findById(discounted.accountId())
            .thenApply(account -> new EnrichedSubscription(discounted, account))
    );
```

---

## Applying to Existing Exercises (q001, q002, q003)

The three existing exercises need to be restructured:

1. **Delete** pre-generated models + interfaces from main package (e.g., `Subscription.java`, `AccountService.java`)
2. **Move** `Solution.java` → `solution/Solution.java` (update package declaration)
3. **Move** all models + interfaces into `solution/` (update package declarations)
4. **Delete** pre-generated `MyAnswerTest.java` from test/
5. **Add** `SolutionTest.java` in `test/.../solution/` (heavily commented)
6. **Strip** `MyAnswer.java` to empty class shell (no imports, no constructor)
7. **Move** `question.md` from `algorithms/completable_future/q00N/` → `algorithms/src/main/java/.../q00N/`

---

## Standard for Future Exercises

Every new CF exercise follows this structure from the start. No exceptions:
- `question.md` lives in `src/main/java/.../<exercise>/`
- Main package: only `MyAnswer.java`
- `solution/` sub-package: all models, interfaces, `Solution.java`
- `test/solution/`: `SolutionTest.java`
