**Lessons:** [0001 Elevator System Domain Model](../../../../../../../../topics/ood/elevator/0001-elevator-domain-model.html)

# q043 — Elevator System

**Category:** OOD · **Difficulty:** Medium · **Ideal time:** 25 min

---

## Problem

Design a simplified single-elevator control system. The elevator has a lifecycle
(idle → moving → door open → idle), and floor requests must be scheduled against a
pluggable dispatch algorithm.

---

## Requirements

1. An `Elevator` has a current floor and a status: `IDLE`, `MOVING`, or `DOOR_OPEN`.
2. Status transitions follow strict rules — use **State Pattern** to model them:
   `IDLE` → (move) → `MOVING` → (arrive) → `DOOR_OPEN` → (closeDoor) → `IDLE`. Any other
   transition attempt must throw `IllegalStateException`.
3. An `ElevatorRequest` captures a requested floor and direction.
4. `ElevatorService` accepts floor requests and, when the elevator is idle, dispatches
   the next request by asking a **Strategy Pattern**-injected `SchedulingStrategy` which
   pending request to service next. Two concrete strategies must exist:
   `FcfsSchedulingStrategy` (first-in-first-out) and `ScanSchedulingStrategy` (nearest
   pending floor to the elevator's current position).
5. When the elevator's door closes, if requests are still pending, the service
   dispatches the next one automatically — no separate trigger required.

---

## Class diagram (skeleton guide)

```
ElevatorState (interface)
  IdleState
  MovingState
  DoorOpenState

SchedulingStrategy (interface)
  FcfsSchedulingStrategy
  ScanSchedulingStrategy

Direction (enum: UP, DOWN)
ElevatorRequest (floor, direction)
Elevator          → holds ElevatorState
ElevatorService → Elevator + SchedulingStrategy + pending List<ElevatorRequest>
```

---

## Constraints

- Arrival is instantaneous in this model — no simulated travel time between floors. This
  is a domain-modelling exercise, not a real-time simulation.
- `ElevatorState` implementations must be the only place that throws `IllegalStateException`
  on an illegal transition — `Elevator` itself only delegates.
- `ElevatorService.closeDoor()` must automatically dispatch the next pending request
  (if any) after the door actually closes — don't require a separate manual call.
- No Spring, no persistence, pure Java 21.

---

## Folder layout

(Standard for `next ood` — see CLAUDE.md's "OOD Delivery Convention" section: full
solution-walkthrough lesson + complete `solution/` + empty `answer/`, mirrored under both
`src/main` and `src/test`.)

```
q043_elevator_system/
├── question.md
├── solution/   ← full reference implementation, one class per file, TDD-ordered
└── answer/     ← empty (.gitkeep) — write from scratch, or study the lesson + solution/ first
```

---

## Hints

If stuck → type `hint`
To see full solution → type `solution`
To submit → type `done`
