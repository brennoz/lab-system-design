package com.interview.ood.q043_elevator_system.solution;

import java.util.ArrayList;
import java.util.List;

// Design decision: orchestrates requests against a single elevator. Mirrors
// q030_library_system's LibraryService: the state object enforces legal transitions, this
// class enforces the business rule of *which* transition happens next (via the injected
// SchedulingStrategy).
//
// Named "Service," not "Controller" — this class has no HTTP/web-layer concerns and isn't
// wired into any framework request-handling pipeline. "Controller" reads as a Spring MVC
// controller in a Java codebase; this is a plain orchestrator, same role and naming
// convention as LibraryService.
//
// Scope note: arrival is modelled as instantaneous — no simulated travel time between
// floors. Same interview-scope simplification as skipping persistence/Spring elsewhere in
// this project's OOD questions; a real system would need MovingState to carry a destination
// and some notion of elapsed time.
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

    // Design decision: called from BOTH requestFloor() and closeDoor(), not just one of
    // them — a pending request can become dispatchable at two different moments: a new
    // request arrives while the elevator happens to already be idle, or the elevator
    // finishes its current trip and frees up. Missing either call site is the single most
    // likely bug in this class: an elevator that services its first request correctly but
    // never picks up a second one queued while it was busy, because nothing re-triggered
    // dispatch when the door closed. SolutionTest's
    // closingDoorDispatchesNextPendingRequestAutomatically test exists specifically to catch
    // that regression.
    //
    // What NOT to do: don't inline the strategy call and the elevator.move()/arrive() pair
    // separately into requestFloor() and closeDoor() instead of sharing this method — that
    // duplication is exactly how the two call sites could drift apart and only one of them
    // gets updated correctly later.
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
