package com.interview.ood.q044_atm_system.solution;

// Design decision (Pattern: State) — the card session's lifecycle (no card / card inserted,
// not yet authenticated / authenticated) is modelled the same way as q043_elevator_system's
// ElevatorState: each status is its own object, only its legal transition is a real no-op,
// everything else throws. Deliberately NOT modelling PIN correctness here — a state only
// answers "is this transition legal from here," not "was the right PIN provided." Credential
// checking is a business decision made by whoever calls authenticate() (see AtmSession),
// same separation as ElevatorState never deciding *which* floor to go to, only whether
// moving is currently legal.
//
// Alternative considered: a single status enum + switch in AtmSession, same rejection as
// every other State-pattern write-up in this project — a real ATM eventually needs more
// states (CARD_RETAINED after 3 failed PIN attempts, OUT_OF_SERVICE), and each one should be
// a new class, not a new switch case scattered across every method that checks status.
//
// What NOT to do: don't let a concrete state reach into AtmSession's account field directly.
// A state's only responsibility is the transition itself (see the concrete classes) —
// account association happens in AtmSession.insertCard(), the same split ElevatorState draws
// between "is the transition legal" (state's job) and "what floor did we arrive at"
// (Elevator.arrive()'s job).
public interface CardState {
    void insertCard(AtmSession session);
    void authenticate(AtmSession session);
    void ejectCard(AtmSession session);
    String name();
}
