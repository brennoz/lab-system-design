package com.interview.ood.q048_skill_based_pay.solution;

import java.time.Duration;

// Thin data record — no behavior. Duration (not a raw number or BigDecimal hours) removes
// any ambiguity about whether totalDuration/breakDuration mean minutes or hours; conversion
// to decimal hours happens once, in StandardShiftPayCalculator, at the point a rate is
// actually applied.
public record Shift(String id, Skill skill, Duration totalDuration, Duration breakDuration) {}
