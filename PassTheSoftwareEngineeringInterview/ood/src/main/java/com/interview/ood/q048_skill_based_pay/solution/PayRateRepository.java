package com.interview.ood.q048_skill_based_pay.solution;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

// EnumMap, not HashMap: both key types (Skill, ContractType) are fixed, closed enums, so
// EnumMap gives a dense array-backed lookup with no hashing — the natural map type once the
// key space is an enum. Rates are injected via constructor (not hardcoded as enum constants)
// so a test can supply its own rate table without touching production defaults, and so a
// real system could later load this from configuration instead of the standardRates()
// factory below.
public class PayRateRepository {

    private final Map<Skill, BigDecimal> skillRates;
    private final Map<ContractType, BigDecimal> contractRates;

    public PayRateRepository(Map<Skill, BigDecimal> skillRates, Map<ContractType, BigDecimal> contractRates) {
        this.skillRates = new EnumMap<>(skillRates);
        this.contractRates = new EnumMap<>(contractRates);
    }

    // Builds the exact rate table from the Tesco exercise spec.
    public static PayRateRepository standardRates() {
        Map<Skill, BigDecimal> skills = new EnumMap<>(Skill.class);
        skills.put(Skill.BAKERY, new BigDecimal("2.00"));
        skills.put(Skill.CHECKOUT_CASHIER, new BigDecimal("1.20"));
        skills.put(Skill.CUSTOMER_SERVICE, new BigDecimal("1.30"));
        skills.put(Skill.SHIFT_LEADER, new BigDecimal("3.00"));
        skills.put(Skill.SECURITY, new BigDecimal("1.00"));
        skills.put(Skill.CLEANING, new BigDecimal("1.00"));
        skills.put(Skill.DELIVERY_DRIVER, new BigDecimal("2.00"));

        Map<ContractType, BigDecimal> contracts = new EnumMap<>(ContractType.class);
        contracts.put(ContractType.PERMANENT, new BigDecimal("12.50"));
        contracts.put(ContractType.AGENCY, new BigDecimal("13.00"));
        contracts.put(ContractType.ZERO_HOURS, new BigDecimal("13.00"));

        return new PayRateRepository(skills, contracts);
    }

    public BigDecimal skillRate(Skill skill) {
        BigDecimal rate = skillRates.get(skill);
        if (rate == null) {
            throw new IllegalStateException("No pay rate configured for skill: " + skill);
        }
        return rate;
    }

    public BigDecimal contractRate(ContractType contractType) {
        BigDecimal rate = contractRates.get(contractType);
        if (rate == null) {
            throw new IllegalStateException("No pay rate configured for contract type: " + contractType);
        }
        return rate;
    }
}
