# Exercise 2 — Invoice Builder (Medium)

**CF methods:** `allOf`, `thenApply` + `join()`
**New concepts:** `completeOnTimeout` (graceful fallback), parallel independent fetches
**Estimated time:** 30 min
**Your file:** `MyAnswer.java` (this directory)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Implement this method in `MyAnswer.java`:

```java
public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId)
```

You need data from three **independent** services — none depends on another's result:
- `SubscriptionService.getSubscription(subscriptionId)` → async
- `BillingAddressService.getBillingAddress(accountId)` → async
- `PaymentMethodService.getDefaultPaymentMethod(accountId)` → async

**Requirement:** If any service takes longer than `timeoutMs`, use a fallback value instead of throwing.

## What to build from scratch

- `Subscription(id, planName, BigDecimal amount)` + `empty()` static factory (the timeout fallback)
- `BillingAddress(accountId, street, city, postCode)` + `unknown()` static factory
- `PaymentMethod(accountId, type, last4)` + `unknown()` static factory
- `Invoice(subscription, billingAddress, paymentMethod)` + `amount()` accessor method
- `interface SubscriptionService`, `BillingAddressService`, `PaymentMethodService` — async contracts
- `*ServiceImpl` — implements each interface; uses `supplyAsync` + virtual thread executor
- `MyAnswer` — constructor takes 3 interfaces + `long timeoutMs`; implements `buildInvoice()`

## Key questions before coding

1. `allOf()` returns `CompletableFuture<Void>` — how do you access the results of your 3 CFs after `allOf` completes?
2. When is it safe to call `join()` on a CF inside a `thenApply` callback?
3. What's the difference between `completeOnTimeout(fallback, ...)` and `orTimeout(...)`? When would you choose each?

## Critical: test parallelism

Your test suite must include a test that verifies all three service calls fire BEFORE any result is awaited. Technique: `AtomicInteger callCount`, return unresolved CFs (`new CompletableFuture<>()`), assert `callCount == 3` after calling `buildInvoice()`.

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q002_invoice_builder.MyAnswerTest"
```
