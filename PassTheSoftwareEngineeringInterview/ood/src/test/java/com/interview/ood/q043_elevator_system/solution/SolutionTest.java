package com.interview.ood.q043_elevator_system.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolutionTest {

    private Elevator elevator;
    private ElevatorService service;

    @BeforeEach
    void initialise() {
        elevator = new Elevator("E1", 0);
        // FCFS by default here: three of the four tests below only need "a" strategy, not
        // a specific one, so FCFS (the simpler of the two) keeps those tests' setup honest
        // about what they're actually exercising — the lifecycle/dispatch wiring, not the
        // scheduling algorithm. The one test that cares about the algorithm
        // (scanSchedulingStrategySelectsNearestPendingRequestFirst) deliberately doesn't use
        // this shared fixture at all — see its own comment below for why.
        service = new ElevatorService(elevator, new FcfsSchedulingStrategy());
    }

    @Test
    void requestFloorMovesElevatorAndOpensDoorAtRequestedFloor() {
        service.requestFloor(5, Direction.UP);

        assertEquals(5, elevator.getCurrentFloor());
        assertEquals("DOOR_OPEN", elevator.getStatus());
    }

    // What this actually verifies: that a SECOND request, queued while the elevator is
    // still busy with the first, gets picked up automatically when the door closes —
    // without any extra manual trigger from the caller. This is the regression test for
    // ElevatorService.dispatchNextIfIdle()'s two call sites (see that method's comment):
    // if closeDoor() ever stopped re-triggering dispatch, this test would start failing
    // with the elevator still parked at floor 5 instead of having moved on to floor 2.
    @Test
    void closingDoorDispatchesNextPendingRequestAutomatically() {
        service.requestFloor(5, Direction.UP);
        service.requestFloor(2, Direction.DOWN);

        service.closeDoor();

        assertEquals(2, elevator.getCurrentFloor());
        assertEquals("DOOR_OPEN", elevator.getStatus());
        assertTrue(service.getPendingRequests().isEmpty());
    }

    // What this verifies: the State pattern's guard is real, not decorative. A weaker
    // version of this test might just check the elevator's status is unchanged after the
    // illegal call — that would also pass if closeDoor() silently no-op'd on an illegal
    // call instead of throwing, which is exactly the failure mode IdleState's comment warns
    // against (a caller bug disappearing instead of surfacing). Asserting the exception
    // itself, not just the elevator's state afterward, is what actually proves the guard
    // fires.
    @Test
    void closingDoorWhileIdleThrowsIllegalStateException() {
        assertEquals("IDLE", elevator.getStatus());

        assertThrows(IllegalStateException.class, elevator::closeDoor);
    }

    // Why this test constructs ScanSchedulingStrategy directly instead of driving it
    // through ElevatorService like the tests above: an earlier version of this test tried
    // to prove "SCAN picks the nearer of two pending requests" by queuing two requests
    // through the service and then asserting on getPendingRequests().size() — but
    // requestFloor() dispatches immediately whenever the elevator is idle, so the very
    // first request always drains before a second one can even be added, making it
    // impossible to get two genuinely pending requests in front of an idle elevator through
    // the public API alone. That version was asserting something the code couldn't
    // actually produce, and would have passed for the wrong reason on either strategy — not
    // a mistake with the strategy itself, but proof that SOME behaviours are only honestly
    // testable at the unit level, not by insisting on going through the same integration
    // path as every other test in this class. Testing the strategy directly, with a
    // hand-built pending list, is the correct scope for this specific claim.
    @Test
    void scanSchedulingStrategySelectsNearestPendingRequestFirst() {
        var strategy = new ScanSchedulingStrategy();
        var requests = List.of(new ElevatorRequest(0, Direction.DOWN), new ElevatorRequest(12, Direction.UP));

        var selected = strategy.selectNext(10, requests);

        // from floor 10: distance to 0 is 10, distance to 12 is 2 — nearest is 12, not FCFS's "first added" (0)
        assertEquals(12, selected.floor());
    }
}
