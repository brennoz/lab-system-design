# elevator — Lesson Index

| File | Concepts covered |
|---|---|
| [0001-elevator-domain-model.html](0001-elevator-domain-model.html) | State Pattern (Elevator lifecycle: IDLE→MOVING→DOOR_OPEN→IDLE), Strategy Pattern (SchedulingStrategy: FcfsSchedulingStrategy vs ScanSchedulingStrategy), core entity design (Elevator vs ElevatorState vs ElevatorService), orchestration (dispatch triggered from both requestFloor() and closeDoor()), record as a value-object choice (ElevatorRequest), common-mistake callout (state pattern self-mutation instead of delegation, same trap as q030) — full solution-walkthrough, first question built under the new standing OOD convention |
