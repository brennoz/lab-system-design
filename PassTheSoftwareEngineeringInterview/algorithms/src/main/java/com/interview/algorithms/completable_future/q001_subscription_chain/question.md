# Exercise 1 — Subscription Chain (Easy)

**CF methods:** `thenApply`, `thenCompose`
**New concepts:** `supplyAsync`, virtual thread executor (`Executors.newVirtualThreadPerTaskExecutor()`)
**Estimated time:** 25 min
**Your file:** `MyAnswer.java` (this directory)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Implement a subscription enrichment pipeline in `MyAnswer.java`:

```java
public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId)
```

Pipeline steps:
1. Fetch `Subscription` by id — **async** (network call)
2. Apply a pricing discount — **synchronous** transform (pure function, no IO)
3. Fetch `Account` using the accountId from the discounted subscription — **async**, depends on step 2
4. Combine into `EnrichedSubscription(discountedSubscription, account)`

## What to build from scratch

Design your own types — use these as a guide:
- `Subscription` — id, accountId, planName, BigDecimal amount
- `DiscountedSubscription` — same identity, different field name for discounted price
- `Account` — id, name, email
- `EnrichedSubscription` — wraps DiscountedSubscription + Account
- `interface SubscriptionService` — `CompletableFuture<Subscription> findById(String id)`
- `SubscriptionServiceImpl implements SubscriptionService` — use `supplyAsync` + `Executors.newVirtualThreadPerTaskExecutor()`
- `interface PricingService` — **synchronous**: `DiscountedSubscription applyDiscount(Subscription s)`
- `PricingServiceImpl implements PricingService` — pure calculation, no executor needed
- `interface AccountService` — `CompletableFuture<Account> findById(String accountId)`
- `AccountServiceImpl implements AccountService` — use `supplyAsync`
- `MyAnswer` — constructor takes all 3 interfaces; implements `enrich()`

## Key questions before coding

- `PricingService.applyDiscount()` is synchronous. `AccountService.findById()` is async.
  Which CF method handles each? What type would you get if you used the wrong one for the async step?
- The account fetch needs `discounted.accountId()` — it can't start until the subscription is fetched.
  Is this fetch parallel or sequential? Which CF method expresses that?

## TDD workflow

1. Write `MyAnswerTest.java` in `src/test/java/.../q001_subscription_chain/`
2. Mock the 3 interfaces with `@Mock`; stub with `CompletableFuture.completedFuture(...)`
3. Red → fail. Implement `enrich()`. Green → pass. Refactor.

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q001_subscription_chain.MyAnswerTest"
```
