package com.interview.ood.q043_elevator_system.solution;

// Design decision (Pattern: State) — each lifecycle status is its own object; only the
// transition(s) legal from that state are implemented as a real no-op, every other method
// throws IllegalStateException. Mirrors q030_library_system's CopyState: the state object
// controls the transition, Elevator only delegates to it.
//
// Alternative considered: a single ElevatorState enum (IDLE, MOVING, DOOR_OPEN) with a
// switch statement in Elevator for every operation. Rejected because every new status (a
// real elevator eventually needs OUT_OF_SERVICE, MAINTENANCE, EMERGENCY_STOP) would mean
// finding and editing every switch block that touches status — exactly the shotgun-surgery
// problem State Pattern exists to avoid. One new state here is one new class; the existing
// three are untouched.
//
// What NOT to do: don't let a concrete state mutate Elevator's fields directly (e.g.
// elevator.currentFloor = x inside a state's move()) — a state's only job is legality of
// the *transition itself* (call setState with the next state, or throw). Floor mutation
// belongs to Elevator.arrive(), which is the one place that changes currentFloor, keeping
// "is this transition allowed" and "what does arriving actually change" as separate
// concerns even though they happen in the same user-visible step.
public interface ElevatorState {
    void move(Elevator elevator);
    void arrive(Elevator elevator);
    void closeDoor(Elevator elevator);
    String name();
}
