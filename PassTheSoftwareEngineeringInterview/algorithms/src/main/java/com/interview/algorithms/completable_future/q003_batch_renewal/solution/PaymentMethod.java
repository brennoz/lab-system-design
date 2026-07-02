package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

// NOTE: q003 PaymentMethod has 'token' not 'last4' (different from q002).
// WHY token: batch renewal charges programmatically against a stored vault token.
//     The token is an opaque reference (e.g. Stripe token "pm_1ABC...").
//     PCI-DSS: tokens are safe to store and transmit; raw card numbers are not.
public record PaymentMethod(String subscriptionId, String type, String token) {}
