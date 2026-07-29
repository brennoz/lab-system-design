package com.interview.ood.q048_skill_based_pay.solution;

// The skills a shift can be worked under, each carrying its own pay differential.
// Same reasoning as ContractType — fixed, closed set, enum over free-text string.
public enum Skill {
    BAKERY,
    CHECKOUT_CASHIER,
    CUSTOMER_SERVICE,
    SHIFT_LEADER,
    SECURITY,
    CLEANING,
    DELIVERY_DRIVER
}
