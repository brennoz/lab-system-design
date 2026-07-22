package com.interview.ood.q043_elevator_system.solution;

import java.util.Comparator;
import java.util.List;

// Design decision: simplified SCAN — service the pending request nearest the elevator's
// current floor, instead of FCFS's request-arrival order. Real SCAN also prefers continuing
// in the elevator's current direction of travel before reversing (the actual "elevator
// algorithm" from OS disk scheduling); this scoped-down nearest-floor-first version is
// enough to demonstrate a genuinely different algorithm from FCFS without needing to also
// track/reason about the elevator's current direction of travel as extra state.
//
// Alternative considered: sort pendingRequests once by distance and cache the order.
// Rejected — "nearest" depends on currentFloor at the moment of dispatch, and currentFloor
// changes after every trip, so a cached sort goes stale after the very first request is
// served. Recomputing per call is the correct cost here, not a missed optimisation.
//
// What NOT to do: don't assume the closest request is always at pendingRequests.get(0) or
// any other fixed index — that's exactly the FCFS behaviour this strategy exists to differ
// from. ElevatorMain's demo deliberately queues floor 18 before floor 3 from current floor 0
// to prove this: FCFS picks 18 (added first), SCAN picks 3 (nearest) — same input list,
// different answer, because the algorithm actually looks at distance instead of position.
public class ScanSchedulingStrategy implements SchedulingStrategy {

    @Override
    public ElevatorRequest selectNext(int currentFloor, List<ElevatorRequest> pendingRequests) {
        return pendingRequests.stream()
                .min(Comparator.comparingInt(request -> Math.abs(request.floor() - currentFloor)))
                .orElseThrow(() -> new IllegalStateException("No pending requests to schedule"));
    }
}
