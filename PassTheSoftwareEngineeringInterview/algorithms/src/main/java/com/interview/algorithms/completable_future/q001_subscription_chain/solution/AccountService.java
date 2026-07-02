package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — account data lives in a separate microservice (remote HTTP/DB call).
// WHY account fetch is SEQUENTIAL (not parallel): we need discounted.accountId() to know
//     WHICH account to fetch. That value is only known after subscription fetch + discount.
//     When a step DEPENDS on the previous result → thenCompose (not thenCombine/allOf).
public interface AccountService {
    CompletableFuture<Account> findById(String accountId);
}
