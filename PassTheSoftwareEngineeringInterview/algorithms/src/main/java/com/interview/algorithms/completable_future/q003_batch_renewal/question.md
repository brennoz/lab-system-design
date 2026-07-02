# Exercise 3 — Batch Subscription Renewal (Hard)

**CF methods:** `thenCombine`, `thenCompose`, `thenApply`, `exceptionally`, `allOf`, `orTimeout`
**New concepts:** Per-item failure isolation, `orTimeout` (hard failure on timeout)
**Estimated time:** 40 min
**Your file:** `MyAnswer.java` (this directory)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Implement batch renewal for a list of subscriptions in `MyAnswer.java`:

```java
public CompletableFuture<List<RenewalResult>> processRenewals(List<String> subscriptionIds)
```

For **each** subscription:
1. Fetch `SubscriptionPlan` and `PaymentMethod` **in parallel** — independent, no dependency between them
2. Once both are ready, **charge** via `ChargeService` — depends on both results
3. If charge succeeds → `RenewalResult.success(subscriptionId, chargeResult)`
4. If charge fails OR times out → `RenewalResult.failed(subscriptionId, errorMessage)` — do NOT let it fail the batch

**Critical:** One failed charge must NOT fail the whole batch. The returned CF must complete normally with a full list even when individual charges fail.

## What to build from scratch

- `SubscriptionPlan(subscriptionId, planName, BigDecimal amount)`
- `PaymentMethod(subscriptionId, type, token)` — token is a payment vault reference
- `ChargeResult(subscriptionId, chargeId, BigDecimal amount)`
- `RenewalResult(subscriptionId, boolean success, ChargeResult, String errorMessage)` + static factories `success()` / `failed()`
- `interface PlanService`, `PaymentMethodService`, `ChargeService` — async contracts
- `*ServiceImpl` for each — `supplyAsync` + virtual thread executor + simulated delay
- `MyAnswer` — constructor takes 3 interfaces + `long timeoutMs`; implements `processRenewals()`

## Key questions before coding

1. Plan and payment method fetches for the SAME subscription are independent. Which CF method waits for exactly 2 parallel CFs and passes both results to a combine function?
2. `chargeService.charge()` returns `CF<ChargeResult>`. If you use `thenApply`, what type do you get? Which CF method fixes this?
3. Should `exceptionally` go on the per-item CF or on the outer `allOf`? What breaks if it goes on the outer one?
4. `orTimeout` vs `completeOnTimeout` — a timed-out charge should be a hard failure (RenewalResult.failed), not a silent fallback. Which is correct?
5. Why must you collect ALL per-item CFs into a List BEFORE calling `allOf`?

## TDD workflow — break into methods

Implement as two methods: `processRenewals()` (outer batch) and `renewOne()` (per-item pipeline). Test each concept separately:
- Test 1: all succeed
- Test 2: one charge fails, others succeed — batch still returns full list (most important)
- Test 3: charge times out (mock returns never-completing CF + 10ms timeout) → `RenewalResult.failed`
- Test 4: result size equals input size

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q003_batch_renewal.MyAnswerTest"
```
