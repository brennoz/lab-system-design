package com.interview.ood.q044_atm_system.solution;

// Only ejectCard() is legal here. What NOT to do: don't add a "re-authenticate" path that
// lets authenticate() succeed again from this state to "refresh" a session — that's a sign
// authentication is being treated as a lease with a timeout, which is a different feature
// (session expiry) this design doesn't claim to model. If that's ever needed, it belongs as
// a new explicit state (e.g. SessionExpiredState) transitioning back through NoCardState,
// not a self-transition bolted onto this one.
public class AuthenticatedState implements CardState {

    @Override
    public void insertCard(AtmSession session) {
        throw new IllegalStateException("Cannot insert card: a card is already inserted");
    }

    @Override
    public void authenticate(AtmSession session) {
        throw new IllegalStateException("Cannot authenticate: already authenticated");
    }

    @Override
    public void ejectCard(AtmSession session) {
        session.setState(new NoCardState());
    }

    @Override
    public String name() {
        return "AUTHENTICATED";
    }
}
