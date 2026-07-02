package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

// NOTE: This PaymentMethod differs from q002's: field is 'token' not 'last4'.
// WHY token: in batch renewal, the charge is made programmatically against a
//     stored payment token (e.g. Stripe token, vault reference).
//     The token allows charging without re-entering card details.
//     It is never the raw card number — tokenisation is a PCI-DSS requirement.
public record PaymentMethod(
    String subscriptionId,  // which subscription this payment method serves
    String type,            // e.g. "CARD", "SEPA_DIRECT_DEBIT"
    String token            // opaque payment vault reference — never a real card number
) {}
