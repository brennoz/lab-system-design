package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

// WHY last4 and not full card number:
//     PCI-DSS (Payment Card Industry Data Security Standard) prohibits storing or
//     transmitting full card numbers in plain text. Only the last 4 digits are safe
//     to display on invoices and UI.
public record PaymentMethod(
    String accountId, // links to the account
    String type,      // e.g. "CARD", "DIRECT_DEBIT", "BANK_TRANSFER"
    String last4      // last 4 digits of card, or last 4 of account number
) {}
