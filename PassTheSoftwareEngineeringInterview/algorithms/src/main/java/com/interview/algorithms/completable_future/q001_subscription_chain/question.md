# Exercise 1 — Subscription Chain (Easy)

**CF methods:** `thenApply`, `thenCompose`
**Estimated time:** 20 min
**Your file:** `MyAnswer.java` (this directory — start from scratch)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Design and implement an enrichment pipeline for a subscription billing system.

Implement a method with this signature in `MyAnswer.java`:

```java
public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId)
```

The pipeline must:
1. Fetch a `Subscription` by id — async (network/DB call)
2. Apply a pricing discount — **synchronous** transform (pure function, no IO)
3. Fetch the `Account` using the `accountId` from the discounted subscription — async, depends on step 2
4. Combine into an `EnrichedSubscription(discountedSubscription, account)`

## What to create (in this package, from scratch)

You decide the field names and types. Here is what the system needs:

- **`Subscription`** — has at minimum: an `id`, an `accountId`, a plan name, and an amount
- **`DiscountedSubscription`** — same identity as Subscription but with a discounted amount field
- **`Account`** — has at minimum: an `id`, a name, an email
- **`EnrichedSubscription`** — combines DiscountedSubscription + Account
- **`SubscriptionService`** — async interface: `CompletableFuture<Subscription> findById(String id)`
- **`PricingService`** — **synchronous** interface: `DiscountedSubscription applyDiscount(Subscription s)`
- **`AccountService`** — async interface: `CompletableFuture<Account> findById(String accountId)`
- **`MyAnswer`** — constructor takes all three services; implements `enrich()`

## Constraints

- Do **not** call `.get()` or `.join()` inside `enrich()` — chain CFs
- Chain from `subscriptionService.findById(subscriptionId)` using CF methods only

## Key question before coding

`PricingService.applyDiscount()` is synchronous. `AccountService.findById()` is asynchronous.
- Which CF method handles a synchronous transform?
- Which CF method handles an async step that depends on the previous result?
- What type would you get if you used the wrong method for the async step?

## TDD workflow

1. Write `MyAnswerTest.java` in `src/test/java/...q001_subscription_chain/`
2. Use `@Mock` for all three services; stub with `CompletableFuture.completedFuture(...)`
3. Run tests — confirm they fail (Red)
4. Implement `MyAnswer.enrich()` — make them pass (Green)
5. Refactor

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q001_subscription_chain.MyAnswerTest"
```

## Hints and reference

- Type `hint` in chat for a nudge
- Type `solution` to reveal the reference implementation
- Or open `solution/Solution.java` directly (but try first!)
