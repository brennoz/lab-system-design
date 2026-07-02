package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// Java: interface — defines a contract without implementation.
// WHY an interface here:
//     1. Testability — Mockito replaces this with a fake that returns controlled CFs
//     2. Hexagonal architecture — domain code depends on this port; infrastructure
//        (HTTP client, database) is a plug-in adapter behind this interface
//     3. The real implementation (e.g. a REST call to the subscription service)
//        never leaks into the exercise
// CF: method returns CompletableFuture<Subscription> — the call is NON-BLOCKING.
//     The caller gets back a CF immediately and can register callbacks
//     (.thenApply, .thenCompose) without blocking the thread.
public interface SubscriptionService {
    // WHY async: subscription lookup requires a network call or DB query.
    //            Async allows the caller to initiate other work (e.g. fetching the Account)
    //            without waiting for this response first.
    CompletableFuture<Subscription> findById(String id);
}
