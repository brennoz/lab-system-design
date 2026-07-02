package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.Executors;

// Runnable demo — shows real async timing with virtual thread executor.
// Run from IDE: right-click → Run 'Main.main()'
public class Main {

    public static void main(String[] args) throws Exception {
        // Java 21: newVirtualThreadPerTaskExecutor — creates one virtual thread per submitted task.
        //          Virtual threads are scheduled by the JVM (not the OS). They park during I/O
        //          (Thread.sleep, HTTP calls, DB queries) without blocking an OS thread.
        //          You can have millions of virtual threads; platform threads max out at ~thousands.
        var executor = Executors.newVirtualThreadPerTaskExecutor();

        var solution = new Solution(
            new SubscriptionServiceImpl(executor, 200),  // simulates 200ms subscription fetch
            new PricingServiceImpl(),                    // sync — instant
            new AccountServiceImpl(executor, 150)        // simulates 150ms account fetch
        );

        System.out.println("Starting q001 enrich pipeline...");
        long start = System.currentTimeMillis();

        var result = solution.enrich("sub-001").get();   // .get() blocks main thread until done

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("Result: %s%n", result);
        // NOTE: account fetch DEPENDS on subscription result → sequential chain
        // Total ≈ 200ms (subscription) + 150ms (account) = ~350ms
        System.out.printf("Time: %dms — sequential chain: 200 + 150 = 350ms expected%n", elapsed);

        executor.shutdown();
    }
}
