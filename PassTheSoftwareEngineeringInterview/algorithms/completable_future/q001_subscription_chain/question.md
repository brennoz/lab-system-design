# Exercise 1 — Subscription Chain (Easy)

**CF methods:** `thenApply`, `thenCompose`
**Estimated time:** 15 min
**Skeleton:** `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswer.java`
**Tests:** `algorithms/src/test/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswerTest.java`

---

## Problem

Implement `MyAnswer.enrich(String subscriptionId): CompletableFuture<EnrichedSubscription>`.

You have three services injected via constructor:
- `SubscriptionService.findById(id)` — async, returns `CompletableFuture<Subscription>`
- `PricingService.applyDiscount(subscription)` — **synchronous**, returns `DiscountedSubscription`
- `AccountService.findById(accountId)` — async, returns `CompletableFuture<Account>`

The pipeline must:
1. Fetch the `Subscription` asynchronously
2. Apply the pricing discount (synchronous transform on the result)
3. Fetch the `Account` using the `accountId` from the discounted subscription (asynchronous — depends on step 2)
4. Combine into `EnrichedSubscription(discountedSubscription, account)`

## Constraints

- Do **not** call `.get()` or `.join()` inside your implementation
- Do **not** use `new CompletableFuture<>()` or `CompletableFuture.supplyAsync()`
- Chain from `subscriptionService.findById(subscriptionId)` using only CF transformation methods
- The `AccountService` call must use `discountedSubscription.accountId()` — not the raw subscription's accountId directly

## Key question to answer

`PricingService.applyDiscount()` is synchronous (returns `DiscountedSubscription`, not `CF<DiscountedSubscription>`).
`AccountService.findById()` is asynchronous (returns `CF<Account>`).

Which CF method do you use for each? Why would using `thenApply` for the account fetch produce the wrong return type?

## Run tests

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="q001_subscription_chain.MyAnswerTest" -f PassTheSoftwareEngineeringInterview/pom.xml
```

## Ask for hints

Type `hint` in chat if stuck. Type `solution` to reveal the reference implementation.
