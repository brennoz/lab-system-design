package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// WHY synchronous: discount calculation is a pure in-memory function — no IO, no network.
// KEY LEARNING — sync vs async determines which CF method the caller uses:
//   sync result  → thenApply(fn)   — fn runs on the CF's completing thread
//   async result → thenCompose(fn) — fn returns CF<U>; thenCompose flattens CF<CF<U>> → CF<U>
//   Using thenCompose for a sync fn would require returning completedFuture(...) — unnecessary.
public interface PricingService {
    DiscountedSubscription applyDiscount(Subscription subscription);
}
