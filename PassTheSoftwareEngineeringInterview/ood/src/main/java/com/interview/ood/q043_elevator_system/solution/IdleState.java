package com.interview.ood.q043_elevator_system.solution;

// Only move() is legal from IDLE. arrive() and closeDoor() throw rather than silently
// no-op-ing — the deliberate choice is: an illegal call is a caller bug, and a thrown
// exception surfaces it immediately at the call site instead of leaving the elevator in a
// state that merely *looks* consistent. A silent no-op here would let, say, a duplicate
// closeDoor() call from a race in the caller vanish without a trace, and the bug would only
// show up later as "the elevator never picked up its next request," far from the actual
// cause.
public class IdleState implements ElevatorState {

    @Override
    public void move(Elevator elevator) {
        elevator.setState(new MovingState());
    }

    @Override
    public void arrive(Elevator elevator) {
        throw new IllegalStateException("Cannot arrive: elevator is not moving");
    }

    @Override
    public void closeDoor(Elevator elevator) {
        throw new IllegalStateException("Cannot close door: elevator door is not open");
    }

    @Override
    public String name() {
        return "IDLE";
    }
}
