package com.interview.ood.q043_elevator_system.solution;

// Pattern: State — each state is an object; only the transition(s) legal from that state
// are implemented as no-ops (they call elevator.setState(...)), every other method throws.
// Mirrors q030_library_system's CopyState: the state object controls the transition,
// Elevator only delegates to it.
public interface ElevatorState {
    void move(Elevator elevator);
    void arrive(Elevator elevator);
    void closeDoor(Elevator elevator);
    String name();
}
