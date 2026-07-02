package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// CF: AccountService is ASYNCHRONOUS — returns CompletableFuture<Account>.
//     This is what requires thenCompose in the pipeline (not thenApply).
// WHY: fetching account data requires an external call (another microservice or DB).
//      The account fetch DEPENDS ON the subscriptionId's accountId — it cannot
//      start until the subscription is known. This sequential dependency is expressed
//      with thenCompose: "when the subscription arrives, start an async account fetch".
public interface AccountService {
    CompletableFuture<Account> findById(String accountId);
}
