package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// CF: PricingService is SYNCHRONOUS — it returns DiscountedSubscription directly,
//     NOT CompletableFuture<DiscountedSubscription>.
// WHY: discount calculation is a pure in-memory function — no IO, no network call.
//      Wrapping a sync function in a CF adds overhead with no benefit.
// KEY LEARNING: this is what determines which CF method the caller uses:
//   - sync transform (no IO)  → thenApply(fn)   — fn runs on the same thread as the CF
//   - async transform (has IO) → thenCompose(fn) — fn returns a new CF, flattening CF<CF<T>>
//   Using thenApply on an async function would give CF<CF<Account>> — double-wrapped, wrong type.
public interface PricingService {
    // ALT: could return Optional<DiscountedSubscription> if some plans have no discount.
    //      Kept simple here — always applies a discount transformation.
    DiscountedSubscription applyDiscount(Subscription subscription);
}
