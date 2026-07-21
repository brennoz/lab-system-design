package com.interview.ood.q043_elevator_system.solution;

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
