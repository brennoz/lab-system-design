package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// Spring Boot: in production, calls the account microservice (REST or gRPC).
// Here: simulates a different latency to account for than the subscription fetch
//       so Main.java timing reflects the sequential nature of this pipeline.
public class AccountServiceImpl implements AccountService {

    private final ExecutorService executor;
    private final long delayMs;

    public AccountServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<Account> findById(String accountId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Account fetch interrupted", e);
            }
            return new Account(accountId, "Acme Corp", "billing@acme.com");
        }, executor);
    }
}
