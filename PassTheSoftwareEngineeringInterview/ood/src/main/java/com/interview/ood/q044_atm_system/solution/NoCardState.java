package com.interview.ood.q044_atm_system.solution;

// Only insertCard() is legal from here. authenticate() and ejectCard() throw rather than
// no-op — same reasoning as q043's IdleState: a caller trying to authenticate with no card
// present, or eject a card that was never inserted, is a bug at the call site, and should
// surface immediately rather than silently doing nothing.
public class NoCardState implements CardState {

    @Override
    public void insertCard(AtmSession session) {
        session.setState(new CardInsertedState());
    }

    @Override
    public void authenticate(AtmSession session) {
        throw new IllegalStateException("Cannot authenticate: no card inserted");
    }

    @Override
    public void ejectCard(AtmSession session) {
        throw new IllegalStateException("Cannot eject: no card inserted");
    }

    @Override
    public String name() {
        return "NO_CARD";
    }
}
