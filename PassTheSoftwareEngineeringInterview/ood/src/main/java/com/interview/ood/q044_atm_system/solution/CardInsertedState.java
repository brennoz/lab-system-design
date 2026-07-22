package com.interview.ood.q044_atm_system.solution;

// Only authenticate() and ejectCard() are legal here — a card is in the machine but PIN
// hasn't been verified yet. ejectCard() being legal from this state (not just from
// AuthenticatedState) matters: a user who inserts a card and changes their mind before
// entering a PIN must be able to get the card back without being forced through
// authentication first.
//
// What NOT to do: don't let insertCard() here silently replace whatever card/account is
// already associated with the session — throwing makes "a second card inserted while one is
// already in the machine" an explicit caller error instead of a card swap nobody asked for.
public class CardInsertedState implements CardState {

    @Override
    public void insertCard(AtmSession session) {
        throw new IllegalStateException("Cannot insert card: a card is already inserted");
    }

    @Override
    public void authenticate(AtmSession session) {
        session.setState(new AuthenticatedState());
    }

    @Override
    public void ejectCard(AtmSession session) {
        session.setState(new NoCardState());
    }

    @Override
    public String name() {
        return "CARD_INSERTED";
    }
}
