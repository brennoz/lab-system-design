package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

// Spring Boot: in production, calls the billing/plan microservice.
// CF: supplyAsync — submits the lambda to the virtual thread executor and returns CF immediately.
// Java 21: newVirtualThreadPerTaskExecutor() — one lightweight virtual thread per task.
//          JVM parks it during Thread.sleep; no OS thread is blocked.
// In production: replace Thread.sleep with restTemplate.getForObject(...) or JPA query.
public class PlanServiceImpl implements PlanService {

    private final ExecutorService executor;
    private final long delayMs;

    public PlanServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<SubscriptionPlan> getPlan(String subscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); throw new RuntimeException(e); }
            return new SubscriptionPlan(subscriptionId, "MONTHLY", BigDecimal.valueOf(50));
        }, executor);
    }
}
