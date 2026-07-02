package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.List;
import java.util.concurrent.Executors;

// Runnable demo — shows batch renewal with all subscriptions processed in parallel.
// Expected: total time ≈ max(plan || pm) + charge, regardless of batch size.
public class Main {

    public static void main(String[] args) throws Exception {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            var solution = new Solution(
                new PlanServiceImpl(executor, 100),           // 100ms plan fetch per sub
                new PaymentMethodServiceImpl(executor, 80),   // 80ms payment method per sub (parallel with plan)
                new ChargeServiceImpl(executor, 200),         // 200ms charge per sub (after plan+pm)
                2000L                                         // 2s timeout per charge
            );

            var subscriptionIds = List.of("sub-001", "sub-002", "sub-003", "sub-004", "sub-005");

            System.out.printf("Processing batch of %d subscriptions...%n", subscriptionIds.size());
            long start = System.currentTimeMillis();

            List<RenewalResult> results = solution.processRenewals(subscriptionIds).get();

            long elapsed = System.currentTimeMillis() - start;
            results.forEach(r -> {
                if (r.success()) {
                    System.out.printf("  OK  %s — chargeId: %s — £%.2f%n",
                        r.subscriptionId(), r.chargeResult().chargeId(), r.chargeResult().amount());
                } else {
                    System.out.printf("  FAIL %s — %s%n", r.subscriptionId(), r.errorMessage());
                }
            });
            // Each subscription: plan(100ms) || pm(80ms) → max=100ms, then charge(200ms) = ~300ms total.
            // 5 subscriptions processed in parallel → still ~300ms (not 5×300ms = 1500ms).
            System.out.printf("Batch complete: %d/%d succeeded in %dms%n",
                results.stream().filter(RenewalResult::success).count(),
                results.size(), elapsed);
        } finally {
            executor.shutdown();
        }
    }
}
