# Exercise 3 — Batch Subscription Renewal (Hard)

**CF methods:** `thenCombine`, `thenCompose`, `thenApply`, `exceptionally`, `allOf`
**Estimated time:** 35 min
**Your file:** `MyAnswer.java` (this directory — start from scratch)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Implement batch renewal for a list of subscriptions.

Implement this method in `MyAnswer.java`:

```java
public CompletableFuture<List<RenewalResult>> processRenewals(List<String> subscriptionIds)
```

You need:
- `PlanService.getPlan(subscriptionId)` → async
- `PaymentMethodService.getPaymentMethod(subscriptionId)` → async
- `ChargeService.charge(plan, paymentMethod)` → async, depends on plan + payment

For **each** subscriptionId:
1. Fetch `SubscriptionPlan` and `PaymentMethod` **in parallel**
2. Once both are ready, charge via `ChargeService`
3. Wrap success as `RenewalResult.success(subscriptionId, chargeResult)`
4. If charge fails, produce `RenewalResult.failed(subscriptionId, errorMessage)` — do NOT propagate the exception

Return `CompletableFuture<List<RenewalResult>>` that completes when ALL renewals are done.

## What to create (in this package, from scratch)

- **`SubscriptionPlan`** — has subscriptionId, planName, amount (BigDecimal)
- **`PaymentMethod`** — has subscriptionId, type, and a payment token
- **`ChargeResult`** — has subscriptionId, chargeId, amount
- **`RenewalResult`** — has subscriptionId, success flag, chargeResult (nullable), errorMessage (nullable)
  - Add static factories: `success(id, chargeResult)` and `failed(id, errorMessage)`
- **`PlanService`**, **`PaymentMethodService`**, **`ChargeService`** — async interfaces
- **`MyAnswer`** — implements `processRenewals()`

## Critical constraint

**One failed charge must NOT fail the whole batch.**
The returned CF must complete normally with a full list where failed items have `success == false`.

This is the key difference from exercise 2 (one failure = whole invoice CF fails).

## Key questions before coding

1. For each subscription: plan and payment method are independent. Which CF method combines two independent CFs into one callback that receives both results?
2. `chargeService.charge()` returns `CF<ChargeResult>`. If you use `thenApply` to call it, what type do you get? How does `thenCompose` fix this?
3. `exceptionally` must be on which CF — the per-item one or the outer batch one? What goes wrong if you put it on the outer one?
4. Why must you collect all per-item CFs into a List BEFORE calling `allOf`?

## TDD workflow

Break into two methods: `processRenewals()` (outer batch) and `renewOne()` (per-item pipeline). Test each concept separately:
- Test 1: all succeed
- Test 2: one fails, others succeed, list still full (most important test)
- Test 3: charge called with correct plan + payment
- Test 4: result list size equals input size

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q003_batch_renewal.MyAnswerTest"
```

## Hints and reference

- Type `hint` in chat for a nudge
- Type `solution` to reveal the reference implementation
