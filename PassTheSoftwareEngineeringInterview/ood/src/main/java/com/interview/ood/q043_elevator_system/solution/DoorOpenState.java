package com.interview.ood.q043_elevator_system.solution;

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
