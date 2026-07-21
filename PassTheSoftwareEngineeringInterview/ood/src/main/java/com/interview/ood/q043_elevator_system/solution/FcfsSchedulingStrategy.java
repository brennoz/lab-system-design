package com.interview.ood.q043_elevator_system.solution;

import java.util.List;

public class FcfsSchedulingStrategy implements SchedulingStrategy {

    @Override
    public ElevatorRequest selectNext(int currentFloor, List<ElevatorRequest> pendingRequests) {
        return pendingRequests.get(0);
    }
}
