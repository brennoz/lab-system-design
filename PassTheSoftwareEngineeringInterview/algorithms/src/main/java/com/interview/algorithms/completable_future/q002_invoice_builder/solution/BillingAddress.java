package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

// WHY separate from Account: billing address is mutable (customer can update it) and
//     lives in its own service. Fetching it independently allows separate caching and
//     independent scaling of the address service vs account service.
public record BillingAddress(String accountId, String street, String city, String postCode) {

    public static BillingAddress unknown() {
        return new BillingAddress("unknown", "N/A", "N/A", "N/A");
    }
}
