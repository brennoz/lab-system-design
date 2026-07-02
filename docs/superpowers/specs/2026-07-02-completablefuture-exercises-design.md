# CompletableFuture Interview Exercises — Design Spec

**Date:** 2026-07-02
**Context:** Interview prep for Zephr/Zuora 3rd technical round — Java CompletableFuture API in TDD style.
**Focus methods:** `allOf`, `thenApply`, `thenCompose`, `thenCombine`

---

## Goal

Three isolated, progressively harder coding exercises in Zuora subscription billing domain. Each exercise:
- Targets specific CF methods
- Has failing tests (Red) that the user makes pass (Green) — TDD workflow
- Uses Mockito to mock async service dependencies
- Has a reference `Solution.java` the user can check after attempting

---

## Location

All files go inside the existing `algorithms` Maven module:

```
PassTheSoftwareEngineeringInterview/
  algorithms/
    src/
      main/java/com/interview/algorithms/completable_future/
        q001_subscription_chain/
        q002_invoice_builder/
        q003_batch_renewal/
      test/java/com/interview/algorithms/completable_future/
        q001_subscription_chain/
        q002_invoice_builder/
        q003_batch_renewal/
```

No new Maven module needed — `algorithms` pom already has JUnit 5, AssertJ, and Mockito (via `spring-boot-starter-test`).

---

## Exercise 1 — Easy — `thenApply` + `thenCompose`

**Package:** `q001_subscription_chain`

**CF methods trained:** `thenApply` (sync transform), `thenCompose` (async dependent step)

### Problem

Implement `SubscriptionEnricher.enrich(String subscriptionId): CompletableFuture<EnrichedSubscription>`.

Pipeline:
1. Call `SubscriptionService.findById(subscriptionId)` → `CompletableFuture<Subscription>`
2. Apply `PricingService.applyDiscount(subscription)` → `DiscountedSubscription` (`thenApply` — sync)
3. Call `AccountService.findById(discounted.getAccountId())` → `CompletableFuture<Account>` (`thenCompose` — async, dependent on step 2 result)
4. Combine into `EnrichedSubscription(discounted, account)` (`thenApply`)

Key learning: `thenApply` when the transform is synchronous. `thenCompose` when the next step returns another `CompletableFuture` — avoids `CF<CF<T>>`.

### Models

```
Subscription(String id, String accountId, String planName, BigDecimal amount)
DiscountedSubscription(String id, String accountId, String planName, BigDecimal discountedAmount)
Account(String id, String name, String email)
EnrichedSubscription(DiscountedSubscription subscription, Account account)
```

### Service interfaces

```java
interface SubscriptionService {
    CompletableFuture<Subscription> findById(String id);
}
interface PricingService {
    DiscountedSubscription applyDiscount(Subscription subscription); // SYNC
}
interface AccountService {
    CompletableFuture<Account> findById(String accountId);
}
```

### Files

| File | Role |
|---|---|
| `Subscription.java` | Record |
| `DiscountedSubscription.java` | Record |
| `Account.java` | Record |
| `EnrichedSubscription.java` | Record |
| `SubscriptionService.java` | Interface |
| `PricingService.java` | Interface |
| `AccountService.java` | Interface |
| `MyAnswer.java` | Skeleton — user implements `enrich()` |
| `Solution.java` | Reference implementation |
| `question.md` | Problem statement + constraints + hint triggers |
| `MyAnswerTest.java` (in test/) | Failing tests |

### Tests (Mockito)

1. **Happy path** — subscription found, discount applied, account fetched → `EnrichedSubscription` with correct fields
2. **thenCompose correctness** — verify `AccountService.findById()` called with `subscription.getAccountId()`, not a hardcoded value
3. **thenApply correctness** — verify discount applied (discounted amount < original)
4. **Subscription not found** — `SubscriptionService` returns failed CF → result CF completes exceptionally

---

## Exercise 2 — Medium — `thenCombine` + `allOf`

**Package:** `q002_invoice_builder`

**CF methods trained:** `thenCombine` (combine exactly 2 CFs), `allOf` (wait for N independent CFs)

### Problem

Implement `InvoiceBuilder.buildInvoice(String subscriptionId, String accountId): CompletableFuture<Invoice>`.

Three **independent** async calls — no result depends on another, so all fire simultaneously:
- `SubscriptionService.getSubscription(subscriptionId)` → `CF<Subscription>`
- `BillingAddressService.getBillingAddress(accountId)` → `CF<BillingAddress>`
- `PaymentMethodService.getDefaultPaymentMethod(accountId)` → `CF<PaymentMethod>`

Combine all three into `Invoice`. Return `CompletableFuture<Invoice>`.

Key learning: when calls are independent, fire all before joining any. `thenCombine` for 2 CFs. `allOf` for 3+. Teach the difference and let the user choose.

### Models

```
Subscription(String id, String planName, BigDecimal amount)
BillingAddress(String accountId, String street, String city, String postCode)
PaymentMethod(String accountId, String type, String last4)
Invoice(Subscription subscription, BillingAddress billingAddress, PaymentMethod paymentMethod, BigDecimal amount)
```

### Service interfaces

```java
interface SubscriptionService {
    CompletableFuture<Subscription> getSubscription(String subscriptionId);
}
interface BillingAddressService {
    CompletableFuture<BillingAddress> getBillingAddress(String accountId);
}
interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getDefaultPaymentMethod(String accountId);
}
```

### Files

Same pattern as ex1 — models, interfaces, `MyAnswer.java`, `Solution.java`, `question.md`, `MyAnswerTest.java`.

### Tests (Mockito)

1. **Happy path** — all 3 complete → `Invoice` with all fields populated correctly
2. **Parallel start** — all 3 CFs initiated before any `join()` (use `CompletableFuture.completedFuture()` stubs; verify call order via `InOrder` or `ArgumentCaptor`)
3. **One service fails** — `BillingAddressService` returns failed CF → result CF completes exceptionally, correct exception propagated
4. **Amount from subscription** — `Invoice.amount` equals `subscription.amount` (no hardcoding)

---

## Exercise 3 — Hard — `allOf` + `thenCombine` + `thenCompose` + `exceptionally`

**Package:** `q003_batch_renewal`

**CF methods trained:** All four, plus `exceptionally` for per-item error isolation

### Problem

Implement `RenewalProcessor.processRenewals(List<String> subscriptionIds): CompletableFuture<List<RenewalResult>>`.

For **each** subscription ID:
1. Fetch `SubscriptionPlan` and `PaymentMethod` **in parallel** → `thenCombine`
2. Attempt charge using plan amount + payment method → `ChargeService.charge(plan, method)` returns `CF<ChargeResult>` → `thenCompose`
3. Wrap successful charge in `RenewalResult.success(id, chargeResult)`
4. Isolate charge failure per item → `exceptionally` → `RenewalResult.failed(id, error)`

Collect all per-item CFs with `allOf`. Return `CompletableFuture<List<RenewalResult>>`.

**One failed charge must NOT affect other items** — the batch always completes with a full result list.

### Models

```
SubscriptionPlan(String subscriptionId, String planName, BigDecimal amount)
PaymentMethod(String subscriptionId, String type, String token)
ChargeResult(String subscriptionId, String chargeId, BigDecimal amount)
RenewalResult(String subscriptionId, boolean success, ChargeResult chargeResult, String errorMessage)
  static RenewalResult success(String id, ChargeResult r)
  static RenewalResult failed(String id, String error)
```

### Service interfaces

```java
interface PlanService {
    CompletableFuture<SubscriptionPlan> getPlan(String subscriptionId);
}
interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getPaymentMethod(String subscriptionId);
}
interface ChargeService {
    CompletableFuture<ChargeResult> charge(SubscriptionPlan plan, PaymentMethod method);
}
```

### Files

Same pattern. `MyAnswer.java` has `processRenewals()` as empty method.

### Tests (Mockito)

1. **All succeed** — 3 subscriptions, all charge successfully → list of 3 `RenewalResult.success`
2. **Partial failure** — 3 subscriptions, middle one's `ChargeService` throws → middle is `RenewalResult.failed`, other two are `success` — list size still 3
3. **Concurrent plan+payment fetch** — for each ID, `PlanService` and `PaymentMethodService` both called before `ChargeService` — verifies parallel inner fetch
4. **All items started** — all 3 subscription pipelines initiated before any result collected (not sequential)

---

## Constraints

- Java 21 records for all model types
- No `Thread.sleep()` in tests — use `CompletableFuture.completedFuture()` / `CompletableFuture.failedFuture()` for Mockito stubs
- No `ExecutorService` management in exercises — use default `ForkJoinPool.commonPool()` or pass as constructor param (solution shows both)
- `question.md` includes: problem statement, CF methods to use, constraints, example input/output, hint trigger instructions (`type "hint" in chat`)
- `Solution.java` is complete and correct — user should NOT read it before attempting
- Tests test `MyAnswer`, not `Solution`
