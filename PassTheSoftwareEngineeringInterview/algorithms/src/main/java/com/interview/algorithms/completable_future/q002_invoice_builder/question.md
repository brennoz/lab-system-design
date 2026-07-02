# Exercise 2 — Invoice Builder (Medium)

**CF methods:** `allOf` (primary), `thenCombine` (alternative)
**Estimated time:** 25 min
**Your file:** `MyAnswer.java` (this directory — start from scratch)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Build an invoice from three independent data sources.

Implement this method in `MyAnswer.java`:

```java
public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId)
```

You need:
- `SubscriptionService.getSubscription(subscriptionId)` → async
- `BillingAddressService.getBillingAddress(accountId)` → async
- `PaymentMethodService.getDefaultPaymentMethod(accountId)` → async

All three calls are **completely independent** — no result depends on another.

## What to create (in this package, from scratch)

- **`Subscription`** — has id, planName, amount (BigDecimal)
- **`BillingAddress`** — has accountId, street, city, postCode
- **`PaymentMethod`** — has accountId, type, and last4 digits
- **`Invoice`** — aggregates all three; add an `amount()` accessor method
- **`SubscriptionService`** — async interface
- **`BillingAddressService`** — async interface
- **`PaymentMethodService`** — async interface
- **`MyAnswer`** — implements `buildInvoice()`

## Constraints

- All three CF calls must start **before** any result is awaited
- Do NOT chain them sequentially — that defeats parallelism
- Test `allThreeFetchesFireBeforeAnyResultIsUsed` in your test suite verifies this

## Key questions before coding

1. `allOf()` returns `CompletableFuture<Void>` — how do you access the results of your 3 CFs after `allOf` completes?
2. When is `join()` safe to call inside a `thenApply` callback?
3. How would `thenCombine` work for this? Where does it get awkward with 3 CFs?

## TDD workflow

1. Write `MyAnswerTest.java` in `src/test/java/...q002_invoice_builder/`
2. Stub all three services with `completedFuture()`
3. Write a parallelism test using `AtomicInteger` + unresolved CFs (see `solution/SolutionTest.java` after you attempt)
4. Red → Green → Refactor

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q002_invoice_builder.MyAnswerTest"
```

## Hints and reference

- Type `hint` in chat for a nudge
- Type `solution` to reveal the reference implementation
