# Exercise 3 — Batch Subscription Renewal (Hard)

**CF methods:** `thenCombine`, `thenCompose`, `thenApply`, `exceptionally`, `allOf`
**Estimated time:** 30 min
**Skeleton:** `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswer.java`
**Tests:** `algorithms/src/test/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswerTest.java`

---

## Problem

Implement `MyAnswer.processRenewals(List<String> subscriptionIds): CompletableFuture<List<RenewalResult>>`.

You have three services injected via constructor:
- `PlanService.getPlan(subscriptionId)` → `CompletableFuture<SubscriptionPlan>`
- `PaymentMethodService.getPaymentMethod(subscriptionId)` → `CompletableFuture<PaymentMethod>`
- `ChargeService.charge(plan, paymentMethod)` → `CompletableFuture<ChargeResult>`

For **each** subscription ID in the input list, you must:
1. Fetch `SubscriptionPlan` and `PaymentMethod` **in parallel** (they're independent)
2. Once both are ready, attempt to charge via `ChargeService.charge(plan, method)`
3. Wrap a successful charge in `RenewalResult.success(subscriptionId, chargeResult)`
4. If the charge fails, produce `RenewalResult.failed(subscriptionId, errorMessage)` — do NOT let the failure propagate to the outer batch

Collect all per-item results. Return a single `CompletableFuture<List<RenewalResult>>` that completes when ALL renewals are done (success or failure).

## Critical constraint

**One failed charge must NOT fail the whole batch.**
The returned CF must complete (not exceptionally) with a full list, where failed items have `success == false`.
This is the key difference between exercise 2 (one failure = whole CF fails) and exercise 3 (per-item isolation).

## Approach hint (without giving it away)

Break the problem into two methods:
- `processRenewals(List<String>)` — launches one CF per ID, uses `allOf` to collect
- `renewOne(String)` — builds the per-item pipeline using `thenCombine` + `thenCompose` + `exceptionally`

## Key questions to answer before coding

1. Why does fetching plan + payment method use `thenCombine` rather than chaining one after the other?
2. `chargeService.charge()` returns `CompletableFuture<ChargeResult>`. If you used `thenApply` to call it, what type would you get? Why does `thenCompose` fix this?
3. `exceptionally` on the per-item CF: what happens if you put it on the OUTER `allOf` CF instead?
4. In `processRenewals`, why do you collect all CFs into a list BEFORE calling `allOf`?

## Run tests

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="com.interview.algorithms.completable_future.q003_batch_renewal.MyAnswerTest" -f PassTheSoftwareEngineeringInterview/pom.xml
```

## Ask for hints

Type `hint` in chat if stuck. Type `solution` to reveal the reference implementation.
