package com.interview.ood.q043_elevator_system.solution;

public class MovingState implements ElevatorState {

    @Override
    public void move(Elevator elevator) {
        throw new IllegalStateException("Cannot move: elevator is already moving");
    }

    @Override
    public void arrive(Elevator elevator) {
        elevator.setState(new DoorOpenState());
    }

    @Override
    public void closeDoor(Elevator elevator) {
        throw new IllegalStateException("Cannot close door: elevator door is not open");
    }

    @Override
    public String name() {
        return "MOVING";
    }
}
