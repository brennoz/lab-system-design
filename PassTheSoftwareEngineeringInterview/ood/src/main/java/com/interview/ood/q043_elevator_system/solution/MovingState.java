package com.interview.ood.q043_elevator_system.solution;

// Only arrive() is legal while MOVING. Note there's no separate "still travelling, not
// there yet" intermediate state — this project's constraint (question.md: "arrival is
// instantaneous, no simulated travel time") means MOVING exists purely to make
// move-while-already-moving and close-door-while-moving illegal, not to model duration.
// A version that simulated real travel time would likely need MOVING to carry a
// destination floor and an ETA, which is explicitly out of scope here.
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
