package com.interview.ood.q044_atm_system.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SolutionTest {

    private Account account;
    private AtmSession session;
    private AtmService service;

    @BeforeEach
    void initialise() {
        account = new Account("ACC-1", new BigDecimal("100.00"));
        session = new AtmSession();
        service = new AtmService(session);
    }

    // What this verifies: the State guard in AtmService is real, not decorative — no card
    // inserted, no PIN verified, yet a command still can't slip through. A weaker test might
    // only check the account balance is unchanged afterward, which would also pass if
    // submitCommand() silently swallowed the attempt instead of throwing — asserting the
    // exception itself is what actually proves the guard fires, same reasoning as q043's
    // closingDoorWhileIdleThrowsIllegalStateException.
    @Test
    void submittingCommandBeforeAuthenticatingThrows() {
        assertThrows(IllegalStateException.class,
                () -> service.submitCommand(new WithdrawCommand(account, new BigDecimal("10.00"))));
    }

    @Test
    void withdrawingWithinBalanceReducesBalance() {
        session.insertCard(account);
        session.authenticate();

        service.submitCommand(new WithdrawCommand(account, new BigDecimal("30.00")));

        // Why compareTo, not assertEquals directly on BigDecimal: BigDecimal.equals() is
        // scale-sensitive ("70.00" != "70.0"), a standing rule in this project.
        assertEquals(0, account.getBalance().compareTo(new BigDecimal("70.00")));
    }

    // What this verifies: Account, not AtmService or WithdrawCommand, is the one place that
    // enforces "no overdraft." Deliberately drives the failure through the full
    // Command -> Account path rather than unit-testing Account.withdraw() in isolation —
    // this is the integration point that actually matters for an ATM (a caller could build
    // a correct WithdrawCommand and still expect the guard to fire).
    @Test
    void withdrawingMoreThanBalanceThrowsInsufficientFunds() {
        session.insertCard(account);
        session.authenticate();

        assertThrows(InsufficientFundsException.class,
                () -> service.submitCommand(new WithdrawCommand(account, new BigDecimal("200.00"))));
    }

    // What this verifies: authentication doesn't persist across a card eject/re-insert
    // cycle. This is the regression test for AuthenticatedState.ejectCard() actually
    // transitioning back to NoCardState (not, say, a state that forgets to reset and leaves
    // the session sitting authenticated for whoever uses the machine next).
    @Test
    void ejectingCardRequiresReAuthenticationBeforeNextCommand() {
        session.insertCard(account);
        session.authenticate();
        session.ejectCard();

        assertThrows(IllegalStateException.class,
                () -> service.submitCommand(new DepositCommand(account, new BigDecimal("10.00"))));
    }
}
