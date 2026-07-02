package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.math.BigDecimal;

// Java: Java 21 record — immutable value object.
// NOTE: This Subscription has only 3 fields (no accountId) — different from q001.
//       In q002, the accountId is passed separately to buildInvoice() by the caller
//       because the invoice builder fetches billing address and payment method by accountId
//       independently of the subscription lookup.
// WHY: In a real billing system, subscriptions and accounts are separate entities.
//      Keeping accountId out of Subscription here models the scenario where the caller
//      already knows the account (e.g. from session context) and uses it for parallel fetches.
public record Subscription(
    String id,
    String planName,
    // Java: BigDecimal for money — no floating-point precision issues at invoice scale.
    BigDecimal amount  // invoice total before tax
) {}
