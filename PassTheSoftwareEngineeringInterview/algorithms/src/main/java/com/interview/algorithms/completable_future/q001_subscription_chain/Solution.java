package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — do not read before attempting MyAnswer.java
public class Solution {

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

    public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {
        return subscriptionService.findById(subscriptionId)           // CF<Subscription>
            .thenApply(pricingService::applyDiscount)                 // CF<DiscountedSubscription> — sync transform
            .thenCompose(discounted ->                                // CF<EnrichedSubscription>   — async step dependent on previous result
                accountService.findById(discounted.accountId())       // CF<Account>
                    .thenApply(account ->                             // sync combine
                        new EnrichedSubscription(discounted, account))
            );
    }
    // thenApply: when the transform is synchronous (pure function, no IO)
    // thenCompose: when the next step itself returns a CompletableFuture
    //   — using thenApply here would give CF<CF<Account>> instead of CF<Account>
}
