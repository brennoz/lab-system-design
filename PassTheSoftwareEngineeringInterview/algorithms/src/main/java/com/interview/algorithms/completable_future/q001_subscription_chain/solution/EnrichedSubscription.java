package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// WHY EnrichedSubscription: this is the output model of the enrichment pipeline.
//     It combines the discounted pricing data with the account metadata needed
//     to generate an invoice or send a billing notification.
// Pattern: Assembler / Enrichment — a common pattern in integration layers
//     where you fetch data from multiple sources and combine into a composite view.
// Java: nested records are perfectly valid. EnrichedSubscription composes two records.
//     Records are value-based (equality by fields), so two EnrichedSubscriptions with
//     identical subscription + account will be equal — useful in tests.
public record EnrichedSubscription(
    DiscountedSubscription subscription, // pricing after discount
    Account account                      // account metadata
) {}
