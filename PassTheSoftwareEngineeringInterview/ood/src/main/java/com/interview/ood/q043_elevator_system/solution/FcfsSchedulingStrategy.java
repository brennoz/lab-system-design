package com.interview.ood.q043_elevator_system.solution;

import java.util.List;

// Design decision: the simplest possible SchedulingStrategy — service requests in the
// order they arrived, ignoring distance entirely. Deliberately naive: it's the baseline
// this question contrasts ScanSchedulingStrategy against (see ElevatorMain's side-by-side
// comparison), and a legitimate real answer when fairness-by-arrival-order matters more
// than total travel efficiency (e.g. a strict SLA on request wait time).
//
// What NOT to do: don't remove pendingRequests.get(0) from the list here. Selecting the
// next request and removing it from the pending queue are two different responsibilities —
// ElevatorService owns the queue and decides when removal happens (after move()/arrive()
// actually run), so a strategy that mutates the list itself would create a hidden
// dependency on call order between selectNext() and whatever removes it.
public class FcfsSchedulingStrategy implements SchedulingStrategy {

    @Override
    public ElevatorRequest selectNext(int currentFloor, List<ElevatorRequest> pendingRequests) {
        return pendingRequests.get(0);
    }
}
