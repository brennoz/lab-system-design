**Lessons:** [0001 ATM System Domain Model](../../../../../../../../topics/ood/atm/0001-atm-domain-model.html)

# q044 — ATM System

**Category:** OOD · **Difficulty:** Medium · **Ideal time:** 25 min

---

## Problem

Design a simplified single-ATM transaction system. A card session has a lifecycle (no card
→ card inserted → authenticated), and once authenticated, transactions are submitted as
discrete, loggable requests rather than direct method calls.

---

## Requirements

1. An `AtmSession` has a status: `NO_CARD`, `CARD_INSERTED`, or `AUTHENTICATED`.
2. Status transitions follow strict rules — use **State Pattern** to model them:
   `NO_CARD` → (insertCard) → `CARD_INSERTED` → (authenticate) → `AUTHENTICATED`.
   `ejectCard` is legal from both `CARD_INSERTED` and `AUTHENTICATED`, always returning to
   `NO_CARD`. Any other transition attempt must throw `IllegalStateException`.
3. An `Account` holds a `BigDecimal` balance — never `double`/`float` for money.
4. A withdrawal, deposit, or balance check is represented as a **Command Pattern** object
   (`AtmCommand` with a single `execute()` method) — `WithdrawCommand`, `DepositCommand`,
   `CheckBalanceCommand`. `AtmService` accepts any `AtmCommand` uniformly via
   `submitCommand()`.
5. `AtmService.submitCommand()` must reject any command with `IllegalStateException` unless
   the session is currently `AUTHENTICATED`.
6. Withdrawing more than the account's balance must throw a dedicated
   `InsufficientFundsException`, thrown by `Account`, not by the command or the service.

---

## Class diagram (skeleton guide)

```
CardState (interface)
  NoCardState
  CardInsertedState
  AuthenticatedState

AtmCommand (interface)
  WithdrawCommand
  DepositCommand
  CheckBalanceCommand

Account              (accountNumber, BigDecimal balance)
InsufficientFundsException
AtmSession            → holds CardState + Account
AtmService             → AtmSession, gates submitCommand() on AUTHENTICATED status
```

---

## Constraints

- `BigDecimal` for all monetary amounts — never `double` or `float`.
- `CardState` implementations must be the only place that throws `IllegalStateException` on
  an illegal session transition — `AtmSession` itself only delegates.
- `AtmService.submitCommand()` must be the single place the authentication guard lives —
  not duplicated into every `AtmCommand` implementation.
- No Spring, no persistence, pure Java 21.

---

## Folder layout

(Standard for `next ood` — see CLAUDE.md's "OOD Delivery Convention" section: full
solution-walkthrough lesson + complete `solution/` (including a runnable `AtmMain.java`) +
empty `answer/`, mirrored under both `src/main` and `src/test`.)

```
q044_atm_system/
├── question.md
├── solution/   ← full reference implementation, one class per file, TDD-ordered, includes AtmMain.java
└── answer/     ← empty (.gitkeep) — write from scratch, or study the lesson + solution/ first
```

---

## Hints

If stuck → type `hint`
To see full solution → type `solution`
To submit → type `done`
