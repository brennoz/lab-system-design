package com.interview.ood.q043_elevator_system.solution;

// Why record: pure immutable data — a requested floor plus the direction the caller wants
// to travel — with no behaviour of its own. Unlike Book (this project's other value-object
// convention), there's no toString()/equals() customisation needed here, so a record is
// the more direct fit than a plain class with hand-written accessors.
public record ElevatorRequest(int floor, Direction direction) {
}
