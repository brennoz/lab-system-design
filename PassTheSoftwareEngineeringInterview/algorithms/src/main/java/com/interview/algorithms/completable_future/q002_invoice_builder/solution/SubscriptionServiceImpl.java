package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// Spring Boot: in production this would be @Service calling a real subscription API.
// Here: simulates latency so Main.java shows all three fetches completing in parallel.
// CF: supplyAsync — submits the lambda to the executor and returns a CF immediately.
//     The calling thread is NOT blocked. When the virtual thread finishes, it
//     completes the CF, triggering any registered callbacks.
// Java 21: newVirtualThreadPerTaskExecutor() — each supplyAsync call gets its own
//          virtual thread (~2KB heap). JVM parks it during sleep; no OS thread blocked.
// In production: replace Thread.sleep with restTemplate.getForObject(...) or JPA query.
public class SubscriptionServiceImpl implements SubscriptionService {

    private final ExecutorService executor;
    private final long delayMs;

    public SubscriptionServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<Subscription> getSubscription(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            return new Subscription(subscriptionId, "ANNUAL", BigDecimal.valueOf(1200));
        }, executor);
    }
}
