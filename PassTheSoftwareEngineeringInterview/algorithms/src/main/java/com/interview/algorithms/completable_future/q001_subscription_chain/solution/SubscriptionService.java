package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// Standard Spring Boot service interface — defines the contract.
// WHY interface: Solution.java depends on this abstraction, not on the concrete Impl.
//     In tests, Mockito creates a proxy that implements this interface.
//     In Main.java, SubscriptionServiceImpl is wired. In production: @Autowired by Spring.
// CF: returns CompletableFuture<Subscription> — non-blocking; the caller gets a CF
//     immediately and can register callbacks (.thenApply, .thenCompose) without blocking.
public interface SubscriptionService {
    CompletableFuture<Subscription> findById(String id);
}
