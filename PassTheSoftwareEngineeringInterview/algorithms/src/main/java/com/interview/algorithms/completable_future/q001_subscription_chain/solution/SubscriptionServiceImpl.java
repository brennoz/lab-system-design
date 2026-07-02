package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// Spring Boot: in production this would be @Service with a real HTTP client or JPA repo.
// Here: simulates network latency with Thread.sleep so Main.java shows real async timing.
// Java 21: executor = newVirtualThreadPerTaskExecutor() — each supplyAsync call gets its own
//          virtual thread. Virtual threads are cheap (~2KB heap vs ~1MB for platform threads).
//          The JVM parks the virtual thread during sleep; no OS thread is blocked.
public class SubscriptionServiceImpl implements SubscriptionService {

    private final ExecutorService executor;
    private final long delayMs;

    // Spring Boot: constructor injection — executor as @Bean, delayMs from @Value("${...}")
    public SubscriptionServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<Subscription> findById(String id) {
        // CF: supplyAsync — submits the lambda to the executor and returns a CF immediately.
        //     The calling thread is NOT blocked. When the virtual thread finishes, it
        //     completes the CF, triggering any registered callbacks (thenApply, etc.).
        // In production: replace Thread.sleep with restTemplate.getForObject(...) or
        //     subscriptionRepository.findById(id) (JPA).
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Subscription fetch interrupted", e);
            }
            return new Subscription(id, "acc-" + id, "ANNUAL", BigDecimal.valueOf(1200));
        }, executor);
    }
}
