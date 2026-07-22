package com.interview.ood.q043_elevator_system.solution;

// Design decision: a record, not a class like Book (this project's other value-object
// precedent). Book carries behaviour-adjacent concerns (a custom toString() via
// ToStringBuilder in q042); ElevatorRequest is pure immutable data with zero behaviour —
// exactly the case a record is built for. Alternative considered: a plain final class with
// hand-written constructor/getters/equals/hashCode, matching Book's style for consistency
// across the project. Rejected here because there's genuinely nothing to hand-write —
// forcing the class form would just be boilerplate with no behavioural payoff, and Java 21
// (this project's target) makes the record the more honest signal of "this is only data."
//
// What NOT to do: don't add behaviour to this class later (a distance() method, a
// isUrgent() check, etc.) just because it's convenient — that's a sign the logic belongs in
// SchedulingStrategy (which already receives the request and the current floor together)
// or in a new collaborator, not smuggled into what should stay a pure data carrier.
public record ElevatorRequest(int floor, Direction direction) {
}
