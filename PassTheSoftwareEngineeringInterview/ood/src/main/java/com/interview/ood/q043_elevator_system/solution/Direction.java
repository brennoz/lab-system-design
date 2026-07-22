package com.interview.ood.q043_elevator_system.solution;

// Design decision: Direction is the *travel direction requested* (UP/DOWN), a property of
// an ElevatorRequest — not the elevator's lifecycle status (IDLE/MOVING/DOOR_OPEN, modelled
// separately by ElevatorState). These are two independent concerns that happen to share the
// word "direction" in casual conversation about elevators, so it's tempting to conflate them.
//
// What NOT to do: don't add IDLE here and reuse this enum as the elevator's status field.
// That would let an Elevator be simultaneously "not moving" (IDLE) and "requested to go UP"
// at the same time with no way to express both — exactly the kind of one-enum-doing-two-jobs
// bug that later forces an awkward null-direction-means-idle special case into every switch
// that touches it. Two enums, two concerns: Direction for what was asked, ElevatorState for
// what's currently happening.
public enum Direction {
    UP, DOWN
}
