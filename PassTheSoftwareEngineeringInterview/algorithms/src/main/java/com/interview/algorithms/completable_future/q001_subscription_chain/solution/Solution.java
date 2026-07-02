package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// This class demonstrates the canonical thenApply + thenCompose pipeline.
// Read the inline comments to understand WHY each CF method was chosen.
public class Solution {

    // WHY constructor injection (not field injection):
    //   1. Testable — pass mocks in tests, real adapters in production
    //   2. Immutable — final fields cannot be reassigned after construction
    //   3. Explicit dependencies — caller knows exactly what this class needs
    private final SubscriptionService subscriptionService;
    private final PricingService pricingService;
    private final AccountService accountService;

    public Solution(SubscriptionService subscriptionService,
                    PricingService pricingService,
                    AccountService accountService) {
        this.subscriptionService = subscriptionService;
        this.pricingService = pricingService;
        this.accountService = accountService;
    }

    // CF pipeline: findById → thenApply(discount) → thenCompose(accountFetch) → thenApply(combine)
    //
    // Return type is CompletableFuture<EnrichedSubscription> — the method returns IMMEDIATELY
    // with a CF that will eventually hold the result. The caller is never blocked.
    public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {

        return subscriptionService.findById(subscriptionId)
            // CF: thenApply — sync transform. PricingService.applyDiscount() is a pure function:
            //     no IO, no network call. thenApply runs the function on the completing thread
            //     and wraps the result in a new CF.
            // WHY NOT thenCompose: applyDiscount returns DiscountedSubscription (not CF<...>).
            //     thenCompose expects a Function<T, CompletableFuture<U>>. Using it for a sync
            //     function would still work, but you'd need to wrap the result in
            //     CompletableFuture.completedFuture(...) — unnecessary overhead.
            .thenApply(pricingService::applyDiscount)          // CF<Subscription> → CF<DiscountedSubscription>

            // CF: thenCompose — async step that DEPENDS on the previous result.
            //     accountService.findById() returns CF<Account> — a new async operation.
            //     thenCompose "flattens" this: instead of CF<CF<Account>>, we get CF<Account>.
            // WHY thenCompose and NOT thenApply:
            //     thenApply(fn) where fn returns CF<Account> would produce CF<CF<Account>>.
            //     That's a nested future — you'd need .get().get() to extract the Account.
            //     thenCompose(fn) automatically unwraps the inner CF, giving CF<Account>.
            .thenCompose(discounted ->                         // CF<DiscountedSubscription> → CF<EnrichedSubscription>
                accountService.findById(discounted.accountId()) // async: CF<Account>
                    // CF: thenApply again — sync combine of discounted + account into output.
                    //     This thenApply is INSIDE the thenCompose lambda, chained on the account CF.
                    .thenApply(account ->
                        new EnrichedSubscription(discounted, account)) // CF<Account> → CF<EnrichedSubscription>
            );
        // ALT: Could use thenCombine if the account fetch were independent of the subscription.
        //      Here it IS dependent (we need discounted.accountId()), so thenCompose is correct.
    }
}
