package com.interview.ood.q043_elevator_system.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolutionTest {

    private Elevator elevator;
    private ElevatorController controller;

    @BeforeEach
    void initialise() {
        elevator = new Elevator("E1", 0);
        controller = new ElevatorController(elevator, new FcfsSchedulingStrategy());
    }

    @Test
    void requestFloorMovesElevatorAndOpensDoorAtRequestedFloor() {
        controller.requestFloor(5, Direction.UP);

        assertEquals(5, elevator.getCurrentFloor());
        assertEquals("DOOR_OPEN", elevator.getStatus());
    }

    @Test
    void closingDoorDispatchesNextPendingRequestAutomatically() {
        controller.requestFloor(5, Direction.UP);
        controller.requestFloor(2, Direction.DOWN);

        controller.closeDoor();

        assertEquals(2, elevator.getCurrentFloor());
        assertEquals("DOOR_OPEN", elevator.getStatus());
        assertTrue(controller.getPendingRequests().isEmpty());
    }

    @Test
    void closingDoorWhileIdleThrowsIllegalStateException() {
        assertEquals("IDLE", elevator.getStatus());

        assertThrows(IllegalStateException.class, elevator::closeDoor);
    }

    @Test
    void scanSchedulingStrategySelectsNearestPendingRequestFirst() {
        var strategy = new ScanSchedulingStrategy();
        var requests = List.of(new ElevatorRequest(0, Direction.DOWN), new ElevatorRequest(12, Direction.UP));

        var selected = strategy.selectNext(10, requests);

        // from floor 10: distance to 0 is 10, distance to 12 is 2 — nearest is 12, not FCFS's "first added" (0)
        assertEquals(12, selected.floor());
    }
}
