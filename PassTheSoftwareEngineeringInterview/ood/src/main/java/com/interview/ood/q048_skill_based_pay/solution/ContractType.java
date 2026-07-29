package com.interview.ood.q048_skill_based_pay.solution;

// The three contract types Tesco pays a different base hourly rate for. A closed,
// fixed set — an enum gives compile-time safety over a free-text "contractType" string
// and lets PayRateRepository use an EnumMap for O(1), always-dense rate lookup.
public enum ContractType {
    PERMANENT,
    AGENCY,
    ZERO_HOURS
}
