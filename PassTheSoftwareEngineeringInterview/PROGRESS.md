# Progress Tracker

| # | Question | Category | Date | Ideal | Actual | Result | Gaps |
|---|----------|----------|------|-------|--------|--------|------|
| q001 | two_sum | algorithms | 2026-05-21 | 20m | — | ✅ Pass | No JUnit tests written; remove in-method comment |
| q002 | url_shortener | system-design | 2026-05-25 | 20m | — | ⏭ Skip | — |
| q003 | valid_parentheses | algorithms | 2026-05-26 | 15m | — | ✅ Pass | No JUnit tests; remove scratch comments; remove main() |
| q005 | design_google_docs | system-design | 2026-05-27 | 30m | — | ⏭ Skip | — |
| q007 | merge_intervals | algorithms | 2026-05-28 | 20m | — | ✅ Pass | main() in solution; dead commented code; prefer for-each |
| q008 | order_service_review | code-review | 2026-05-28 | 15m | — | ❌ Fail | Missed: SimpleDateFormat thread-safety, N+1 query, HashMap distributed pitfall, swallowed exception, missing @Transactional |
| q009 | best_time_buy_sell_stock | algorithms | 2026-05-28 | 15m | — | ✅ Pass | l++ should be l=r; remove main(); inline profit var |
| q010 | payment_consumer_review | code-review | 2026-05-29 | 15m | — | ❌ Fail | Missed: == on String, swallowed exception, PCI log violation, static counter race condition |
| q011 | maximum_subarray | algorithms | 2026-05-29 | 20m | — | ✅ Pass | Remove main() |
| q012 | user_service_review | code-review | 2026-05-29 | 15m | — | ❌ Fail | Missed: SQL injection (critical), Optional.get() no check, plain text password, entity as DTO |
| q013 | parking_lot | ood | 2026-06-01 | 25m | — | ✅ Pass | park()/leave() not synchronized; add() null check wrong; fill in design decisions block |
| q014 | report_service_review | code-review | 2026-06-01 | 15m | — | ❌ Fail | Missed: == on String (3rd time!), swallowed exception stack trace, missing @Transactional |
| q015 | payment_processor_review | code-review | 2026-06-02 | 15m | — | ❌ Fail | Missed: race condition/optimistic lock (overdraft), no idempotency key (double charge) — FINALLY caught == String |
| q016 | transfer_service_review | code-review | 2026-06-02 | 15m | — | ❌ Fail | Missed: N+1 query, Optional.get() no check, entity as DTO — excellent bonus: Outbox + @Version caught |
| q017 | payment_endpoint_review | code-review | 2026-06-03 | 12m | — | ❌ Fail | Missed: entity as DTO (again); idempotency half-credit; caught Optional.get() + constructor injection bonus |
| q021 | payment_order_review | code-review | 2026-06-04 | 12m | — | ✅ Pass | First clean pass: caught entity as DTO + idempotency + Optional.get(); bonus @Transactional + constructor injection |
| q023 | wallet_service_review | code-review | 2026-06-09 | 15m | — | ❌ Fail | Missed: SimpleDateFormat thread-safety, entity as DTO (regressed), N+1 query; caught @Version + @Transactional |
| q024 | bet_placement_review | code-review | 2026-06-09 | 15m | — | ❌ Fail | Missed: double for money (betting trigger), idempotency (regression); caught Redis liability counter + @Transactional |
| q025 | savings_account_review | code-review | 2026-06-10 | 12m | — | ✅ Pass | Caught: Optional.get(), entity as DTO (both methods), double for money; bonus @Transactional + idempotency |
| q018 | team_formation_efficiency | algorithms | 2026-06-04 | 20m | — | ✅ Pass | main() in solution; scratch comment block; temp==0 sentinel fragile — prefer explicit target var |
| q038 | loan_disbursement_review | code-review | 2026-07-08 | — | — | ✅ Pass | 5/5: ==String, double, entity-DTO, @Transactional, missing audit log; missed: @GetMapping on state-change |
| q040 | payment_wallet | ood | 2026-07-09 | 20m | — | ⏳ Open | — |
| q041 | bet_settlement_pipeline | devops-architecture | 2026-07-10 | 45m | — | ⏳ Open | — |
| q042 | library_search | ood | 2026-07-20 | 20m | — | ⚠️ Retro→Fixed | Real NatWest 3rd-round take-home: search() ignored searchString entirely. Froze live on implementation + scope uncertainty + calibration gap vs richer q030/ex4 prep. Fixed post-interview using Specification pattern (BookSpecification + Title/Author/Or) instead of an inline filter — ticket's title-OR-author shape signaled more criteria were coming. First question built under the new standing OOD convention: full lesson (0002, 4 search-approach layers) + solution/ + empty answer/. |
| q030 | library_system | ood | 2026-06-15 | 25m | — | ⚠️ Partial→Fixed | Initial review 2/4: (1) State pattern not wired — BookCopy mutated `state` field directly instead of delegating to `state.reserve(this)`, so every guard was dead code; (2) `fine == BigDecimal.ZERO` reference-equality bug + inverted ternary. Both fixed in place post-review to 4/4. Logged retroactively; restructured into solution/+answer/attempt1/ 2026-07-15 |
| q043 | elevator_system | ood | 2026-07-21 | 25m | — | ⏳ Open | Second question built under the new standing OOD convention: lesson 0001 (State Pattern for Elevator lifecycle, Strategy Pattern for FcfsSchedulingStrategy vs ScanSchedulingStrategy) + full solution/ + empty answer/, mirrored under both src/main and src/test. |
