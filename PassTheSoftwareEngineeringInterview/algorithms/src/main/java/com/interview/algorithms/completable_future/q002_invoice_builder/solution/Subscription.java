package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.math.BigDecimal;

// NOTE: q002 Subscription has NO accountId field — accountId is passed separately to
//       buildInvoice(). The three service calls (subscription, address, payment) are all
//       INDEPENDENT — none depends on another's result. That independence is what makes
//       allOf() the right tool (vs thenCompose which is for sequential dependencies).
public record Subscription(String id, String planName, BigDecimal amount) {

    // WHY static factory: provides a named fallback value when the service times out.
    //     completeOnTimeout(Subscription.empty(), ...) is self-documenting at the call site.
    public static Subscription empty() {
        return new Subscription("unknown", "UNKNOWN", BigDecimal.ZERO);
    }
}
