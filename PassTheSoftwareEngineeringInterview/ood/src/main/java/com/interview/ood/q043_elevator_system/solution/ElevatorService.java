package com.interview.ood.q043_elevator_system.solution;

import java.util.ArrayList;
import java.util.List;

// Orchestrates requests against a single elevator. Mirrors q030_library_system's
// LibraryService: the state object enforces legal transitions, this class enforces the
// business rule of *which* transition happens next (via the injected SchedulingStrategy).
// Scope note: arrival is modelled as instantaneous — no simulated travel time between
// floors — same interview-scope simplification as skipping persistence/Spring elsewhere
// in this project's OOD questions.
public class ElevatorService {

    private final Elevator elevator;
    private final SchedulingStrategy strategy;
    private final List<ElevatorRequest> pendingRequests = new ArrayList<>();

    public ElevatorService(Elevator elevator, SchedulingStrategy strategy) {
        this.elevator = elevator;
        this.strategy = strategy;
    }

    public void requestFloor(int floor, Direction direction) {
        pendingRequests.add(new ElevatorRequest(floor, direction));
        dispatchNextIfIdle();
    }

    public void closeDoor() {
        elevator.closeDoor();
        dispatchNextIfIdle();
    }

    private void dispatchNextIfIdle() {
        if (!"IDLE".equals(elevator.getStatus()) || pendingRequests.isEmpty()) {
            return;
        }
        ElevatorRequest next = strategy.selectNext(elevator.getCurrentFloor(), pendingRequests);
        pendingRequests.remove(next);
        elevator.move();
        elevator.arrive(next.floor());
    }

    public Elevator getElevator() {
        return elevator;
    }

    public List<ElevatorRequest> getPendingRequests() {
        return pendingRequests;
    }
}
