package com.interview.ood.q043_elevator_system.solution;

public class Elevator {

    private final String id;
    private int currentFloor;
    // Design decision: state held as an object, not an enum + switch — transitions are
    // encapsulated in the state itself (IdleState/MovingState/DoorOpenState), Elevator only
    // delegates. See ElevatorState.java for the full alternatives-considered discussion.
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

    // Design decision: package-private, not public. Only ElevatorState implementations
    // (same package) may call this — a state object has already validated the transition is
    // legal by the time it calls setState, so no caller outside the state hierarchy should
    // ever need to.
    //
    // What NOT to do: don't make this public "for convenience" (e.g. to let a test or a
    // caller force an elevator into DOOR_OPEN directly). That reintroduces exactly the bug
    // this whole design prevents — an arbitrary caller silently bypassing every guard in
    // IdleState/MovingState/DoorOpenState, the same "state pattern wired but bypassed"
    // mistake flagged in q030_library_system's grading history (BookCopy mutating its state
    // field directly instead of delegating).
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
