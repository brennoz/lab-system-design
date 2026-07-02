# Exercise 2 — Invoice Builder (Medium)

**CF methods:** `allOf` (primary), `thenCombine` (alternative)
**Estimated time:** 20 min
**Skeleton:** `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswer.java`
**Tests:** `algorithms/src/test/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswerTest.java`

---

## Problem

Implement `MyAnswer.buildInvoice(String subscriptionId, String accountId): CompletableFuture<Invoice>`.

You have three services injected via constructor — all async:
- `SubscriptionService.getSubscription(subscriptionId)` → `CompletableFuture<Subscription>`
- `BillingAddressService.getBillingAddress(accountId)` → `CompletableFuture<BillingAddress>`
- `PaymentMethodService.getDefaultPaymentMethod(accountId)` → `CompletableFuture<PaymentMethod>`

These three calls are **completely independent**. No result depends on another.

Fetch all three simultaneously and combine them into `new Invoice(subscription, billingAddress, paymentMethod)`.

## Constraints

- All three CF calls must be **initiated before any result is awaited**
- Do NOT call `.get()` or `.join()` before all three CFs have been started
- Do NOT chain them sequentially (subscription → billing → payment would be 3× slower)
- The test `allThreeFetchesStartBeforeAnyResultIsUsed` verifies parallelism explicitly

## Key question to answer

`allOf` returns `CompletableFuture<Void>` — it does not carry the results.
How do you access the results of `subCf`, `addrCf`, `pmCf` inside the `thenApply` after `allOf`?
(Hint: what does `allOf` guarantee about those CFs by the time `thenApply` runs?)

## thenCombine vs allOf

`thenCombine(cf2, (r1, r2) -> combine(r1, r2))` works perfectly for exactly 2 CFs.
For 3+ independent CFs, `allOf` is cleaner — no nesting required.
Try implementing with `thenCombine` first, then refactor to `allOf`.

## Run tests

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="q002_invoice_builder.MyAnswerTest" -f PassTheSoftwareEngineeringInterview/pom.xml
```

## Ask for hints

Type `hint` in chat if stuck. Type `solution` to reveal the reference implementation.
