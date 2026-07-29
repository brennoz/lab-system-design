package com.interview.ood.q048_skill_based_pay.solution;

import java.util.List;

// Thin data record — no pay-calculation logic here. PayrollService owns the calculation,
// same "domain objects are data containers, service classes own logic" split this project
// uses throughout (see SocialGraph/LikeService vs Follow/Like in q035).
public record Employee(String id, ContractType contractType, List<Shift> shifts) {
    public Employee {
        shifts = List.copyOf(shifts);
    }
}
