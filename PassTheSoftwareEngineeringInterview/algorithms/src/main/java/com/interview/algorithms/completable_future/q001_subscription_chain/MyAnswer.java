package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.util.concurrent.CompletableFuture;

// Exercise 1: thenApply + thenCompose
// See concepts/completable_future/q001_subscription_chain/question.md for the full problem statement.
public class MyAnswer {

    private final SubscriptionService subscriptionService;
    private final PricingService pricingService;
    private final AccountService accountService;

    public MyAnswer(SubscriptionService subscriptionService,
                    PricingService pricingService,
                    AccountService accountService) {
        this.subscriptionService = subscriptionService;
        this.pricingService = pricingService;
        this.accountService = accountService;
    }

    /**
     * Build an EnrichedSubscription by:
     * 1. Fetching Subscription by id (async)
     * 2. Applying a pricing discount (sync transform)   ← use thenApply
     * 3. Fetching Account using the subscription's accountId (async, dependent) ← use thenCompose
     * 4. Combining into EnrichedSubscription
     *
     * @return CompletableFuture<EnrichedSubscription>
     */
    public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {
        throw new UnsupportedOperationException("Implement me — see question.md");
    }
}
