package com.interview.ood.q043_elevator_system.solution;

import java.util.List;

// Design decision (Pattern: Strategy) — the algorithm for picking which pending request to
// service next is injected into ElevatorService, not hardcoded. Mirrors q030_library_system's
// FineCalculationStrategy: swap the algorithm without touching the orchestrator.
//
// Alternative considered: an if/else or a "scheduling mode" enum branch inside
// ElevatorService.dispatchNextIfIdle() itself. Rejected for the same Open/Closed reason as
// every Strategy in this project — adding a third scheduling algorithm would mean editing
// ElevatorService's orchestration method directly, risking breaking the dispatch logic that
// has nothing to do with the algorithm choice.
//
// What NOT to do: don't give an implementation a stored `currentFloor` or `elevatorId`
// field. selectNext() receives currentFloor as a parameter specifically so one strategy
// instance is stateless and reusable across every Elevator/ElevatorService pair in a
// building, rather than needing a fresh strategy object per elevator.
public interface SchedulingStrategy {
    ElevatorRequest selectNext(int currentFloor, List<ElevatorRequest> pendingRequests);
}
