package com.interview.ood.q043_elevator_system.solution;

// Only closeDoor() is legal while DOOR_OPEN. What NOT to do: don't fold "door open" and
// "idle" into a single status just because both are, loosely, "not moving." They're
// distinct on purpose — ElevatorService.dispatchNextIfIdle() checks for "IDLE" specifically
// (not "not moving"), so both MOVING and DOOR_OPEN block a new dispatch, guaranteeing a
// rider a window to board/exit before the next request can steal the elevator away. Merging
// DOOR_OPEN into IDLE would remove that guarantee silently — a new request could dispatch
// while the door is still open and someone is mid-boarding.
public class DoorOpenState implements ElevatorState {

    @Override
    public void move(Elevator elevator) {
        throw new IllegalStateException("Cannot move: door is open");
    }

    @Override
    public void arrive(Elevator elevator) {
        throw new IllegalStateException("Cannot arrive: elevator is not moving");
    }

    @Override
    public void closeDoor(Elevator elevator) {
        elevator.setState(new IdleState());
    }

    @Override
    public String name() {
        return "DOOR_OPEN";
    }
}
