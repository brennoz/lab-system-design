package com.interview.ood.q043_elevator_system.solution;

public class Elevator {

    private final String id;
    private int currentFloor;
    // Why held as object, not enum + switch: transitions are encapsulated in the state
    // itself (see IdleState/MovingState/DoorOpenState) — Elevator only delegates.
    private ElevatorState state = new IdleState();

    public Elevator(String id, int currentFloor) {
        this.id = id;
        this.currentFloor = currentFloor;
    }

    public void move() {
        state.move(this);
    }

    public void arrive(int newFloor) {
        state.arrive(this);
        this.currentFloor = newFloor;
    }

    public void closeDoor() {
        state.closeDoor(this);
    }

    void setState(ElevatorState state) {
        this.state = state;
    }

    public String getStatus() {
        return state.name();
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public String getId() {
        return id;
    }
}
