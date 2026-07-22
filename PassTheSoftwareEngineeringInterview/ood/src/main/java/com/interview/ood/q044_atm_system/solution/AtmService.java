package com.interview.ood.q044_atm_system.solution;

// Design decision: orchestrates command execution against a session, gating on
// authentication state — the same "State enforces legality, Service enforces business
// rules on top" split as q043's ElevatorService, applied to Command instead of a
// dispatch queue. Named "Service," not "Controller" — see q043's ElevatorService for why
// that naming distinction matters in this project (no HTTP/web-layer concerns here).
//
// What NOT to do: don't move the "must be authenticated" check into AtmCommand or its
// implementations. That would mean every future command type has to remember to add the
// same guard, and a command tested in isolation (unit-testing WithdrawCommand.execute()
// directly) would need a fake authenticated session just to exercise logic that has
// nothing to do with authentication. The guard belongs at the one place all commands pass
// through.
public class AtmService {

    private final AtmSession session;

    public AtmService(AtmSession session) {
        this.session = session;
    }

    public void submitCommand(AtmCommand command) {
        if (!"AUTHENTICATED".equals(session.getStatus())) {
            throw new IllegalStateException("Cannot submit a transaction before authenticating");
        }
        command.execute();
    }
}
