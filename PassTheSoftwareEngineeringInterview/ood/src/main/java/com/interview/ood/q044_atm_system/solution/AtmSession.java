package com.interview.ood.q044_atm_system.solution;

public class AtmSession {

    // Design decision: state held as an object, not an enum + switch — see CardState.java
    // for the full alternatives-considered discussion. Same shape as q043's Elevator.
    private CardState state = new NoCardState();
    private Account account;

    public void insertCard(Account account) {
        state.insertCard(this);
        this.account = account;
    }

    public void authenticate() {
        state.authenticate(this);
    }

    public void ejectCard() {
        state.ejectCard(this);
        // Design decision: clear the account reference on eject, not just the state. What
        // NOT to do: leaving `account` set after eject would mean a bug elsewhere that
        // bypasses the state guard (e.g. a future method added carelessly) could still
        // silently operate against the previous customer's account — clearing it here means
        // there's no account to operate against even if a guard is ever missed.
        this.account = null;
    }

    // Design decision: package-private, same reasoning as Elevator.setState() in q043 —
    // only CardState implementations (same package) have already validated a transition is
    // legal by the time they call this. What NOT to do: don't widen this to public "so tests
    // can force a state" — that's the exact bypass q030's grading history flagged (BookCopy
    // mutating its own state field instead of delegating through the state object).
    void setState(CardState state) {
        this.state = state;
    }

    public String getStatus() {
        return state.name();
    }

    public Account getAccount() {
        return account;
    }
}
