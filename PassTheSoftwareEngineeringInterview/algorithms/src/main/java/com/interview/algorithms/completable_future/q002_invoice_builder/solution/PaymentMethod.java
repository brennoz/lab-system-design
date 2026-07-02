package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

// WHY last4 and not full card: PCI-DSS (Payment Card Industry Data Security Standard)
//     prohibits storing or transmitting full card numbers in plain text.
//     Only the last 4 digits are safe to display on invoices and in logs.
public record PaymentMethod(String accountId, String type, String last4) {

    public static PaymentMethod unknown() {
        return new PaymentMethod("unknown", "UNKNOWN", "0000");
    }
}
