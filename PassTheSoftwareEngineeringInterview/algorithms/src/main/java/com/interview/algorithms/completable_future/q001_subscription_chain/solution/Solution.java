package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// Pipeline: findById → thenApply(discount) → thenCompose(accountFetch) → thenApply(combine)
// Spring Boot: this class would be @Service, called by a @RestController.
public class Solution {

    // Spring Boot: @Autowired constructor injection — Spring resolves each interface to its @Service impl.
    // WHY constructor injection (not field @Autowired): testable, immutable (final fields), explicit.
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

    // Returns CF immediately — never blocks the calling thread.
    // Spring Boot: controller returns Mono<ResponseEntity> (WebFlux) or calls .get() in a @Async method.
    public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {
        return subscriptionService.findById(subscriptionId)
            // CF: thenApply — sync transform. PricingService.applyDiscount() has no IO.
            //     Runs on the thread that completed subscriptionService.findById().
            //     Returns CF<DiscountedSubscription> wrapping the sync result.
            // WHY NOT thenCompose: applyDiscount() returns DiscountedSubscription (not CF<...>).
            //     thenCompose expects Function<T, CompletableFuture<U>> — wrong tool for sync work.
            .thenApply(pricingService::applyDiscount)

            // CF: thenCompose — async step that DEPENDS on the previous result.
            //     accountService.findById() returns CF<Account> — a new async operation.
            //     WHY NOT thenApply: that would give CF<CF<Account>> (double-wrapped).
            //     thenCompose unwraps: CF<CF<Account>> → CF<Account>.
            .thenCompose(discounted ->
                accountService.findById(discounted.accountId())
                    // thenApply inside thenCompose: sync combine of both results.
                    .thenApply(account -> new EnrichedSubscription(discounted, account))
            );
    }
}
