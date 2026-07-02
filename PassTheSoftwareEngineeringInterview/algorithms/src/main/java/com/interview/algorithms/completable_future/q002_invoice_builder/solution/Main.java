package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.Executors;

// Runnable demo — shows all three services completing in parallel.
// Expected output: total time ≈ max(200, 180, 150) = ~200ms (not 530ms sequential).
public class Main {

    public static void main(String[] args) throws Exception {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            var solution = new Solution(
                new SubscriptionServiceImpl(executor, 200),    // 200ms
                new BillingAddressServiceImpl(executor, 180),  // 180ms — runs IN PARALLEL with others
                new PaymentMethodServiceImpl(executor, 150),   // 150ms — runs IN PARALLEL with others
                500L                                           // 500ms timeout per service
            );

            System.out.println("Starting q002 buildInvoice — 3 parallel fetches...");
            long start = System.currentTimeMillis();

            Invoice invoice = solution.buildInvoice("sub-001", "acc-001").get();

            long elapsed = System.currentTimeMillis() - start;
            System.out.printf("Invoice amount: %s%n", invoice.amount());
            System.out.printf("Billing address: %s%n", invoice.billingAddress());
            System.out.printf("Payment: %s *%s%n", invoice.paymentMethod().type(), invoice.paymentMethod().last4());
            // All 3 fire simultaneously → total ≈ max(200, 180, 150) = 200ms
            System.out.printf("Time: %dms — parallel: expected ~200ms (max of 200, 180, 150)%n", elapsed);
        } finally {
            executor.shutdown();
        }
    }
}
