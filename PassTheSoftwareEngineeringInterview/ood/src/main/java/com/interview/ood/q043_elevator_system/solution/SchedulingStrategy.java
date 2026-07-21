package com.interview.ood.q043_elevator_system.solution;

import java.util.List;

// Pattern: Strategy — the algorithm for picking which pending request to service next is
// injected, not hardcoded into ElevatorController. Mirrors q030_library_system's
// FineCalculationStrategy: swap the algorithm without touching the orchestrator.
public interface SchedulingStrategy {
    ElevatorRequest selectNext(int currentFloor, List<ElevatorRequest> pendingRequests);
}
