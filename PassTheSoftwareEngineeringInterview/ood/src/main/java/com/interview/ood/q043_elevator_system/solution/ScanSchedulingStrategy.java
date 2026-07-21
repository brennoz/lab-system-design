package com.interview.ood.q043_elevator_system.solution;

import java.util.Comparator;
import java.util.List;

// Simplified SCAN: service the pending request nearest the elevator's current floor first,
// instead of FCFS's request-arrival order. Real SCAN also prefers continuing in the
// elevator's current direction before reversing — nearest-floor-first is the scoped-down
// version of that idea, enough to demonstrate a genuinely different algorithm from FCFS.
public class ScanSchedulingStrategy implements SchedulingStrategy {

    @Override
    public ElevatorRequest selectNext(int currentFloor, List<ElevatorRequest> pendingRequests) {
        return pendingRequests.stream()
                .min(Comparator.comparingInt(request -> Math.abs(request.floor() - currentFloor)))
                .orElseThrow(() -> new IllegalStateException("No pending requests to schedule"));
    }
}
