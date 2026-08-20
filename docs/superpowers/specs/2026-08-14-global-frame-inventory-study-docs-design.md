# Design: Global Frame Inventory — arm-pythia-style study docs

## Goal

Produce a set of study documents, in the style of `topics/projects/arm-pythia/`, covering
the user's own implementation of the Global "Frame Inventory" take-home interview exercise.
The docs are for the user's own review/study — not interview prep for a future round, not a
submission artifact. They need to be dense with real code excerpts + explanation, not prose
summaries, so the user can review their own build critically.

## Source materials

- **Original exercise skeleton** (what Global provided):
  `/home/brennoz77/LABS/LAB_INTERVIEWS/original/frame-inventory-exercise-main/`
  — `INSTRUCTIONS.md` (full brief), `data/inventory_frame.csv` (~1,300-row legacy export),
  `design-system/` (the frontend design system to build against), a suggested
  `docker-compose.yml` (backend + frontend + MongoDB + MariaDB + OpenSearch/Dashboards —
  explicitly "a starting point, not a requirement").

- **The user's solution** (what was actually built):
  `/home/brennoz77/LABS/LAB_INTERVIEWS/frame-inventory-exercise-main/`
  (public repo: `https://github.com/brennoz/frame-inventory-exercise-main`)
  — Spring Boot 3.5 / Java 21 backend (`src/backend`, 39 main Java files across
  `frame/{model,dto,service,repository,controller,exception}`, `web`, `config`,
  `observability`, `health`), React/TypeScript/Vite frontend (`src/frontend`, 28 files),
  MariaDB-only `docker-compose.yml`, and an existing `docs/` folder already containing:
  - 3 ADRs (`docs/adr/0001` relational persistence & SQL search, `0002` synchronous
    transactional CSV import, `0003` optimistic concurrency & transactional audit history)
  - `docs/implementation-journal.md` — reconstructed gate-by-gate delivery record with real
    commit hashes, since Entire failed to capture Codex checkpoints
  - `docs/conversation-record.md` — chronological request/outcome record
  - `README.md` — architecture table, API table, testing instructions, limitations

These existing docs are treated as **source of truth for the "why"** — the new study docs
expand and illustrate them with real code, not re-derive or second-guess the reasoning from
scratch. Where the new docs go further than the existing ADRs (the two "what if" pages, the
production roadmap), that's new synthesis built on top of, and consistent with, the existing
ADR reasoning (e.g. ADR 0001's "a search projection can be added ... through a transactional
outbox rather than dual writes" is the seed for page 10 and 11).

Already-verified during brainstorming (read directly from source, not assumed):
- `Frame.java` — JPA entity, `@Version` optimistic lock, immutable `frameId`, `create`/`update`
  factory methods, `FrameData` value object round-trip via `data()`/`apply()`.
- `FrameCommandService.java` — `create`/`update`/`upsertImportedFrame`; catches
  `ObjectOptimisticLockingFailureException` → `StaleFrameVersionException`; CSV path uses
  `@Transactional(propagation = REQUIRES_NEW)` per row; `saveAndFlush`/`flush()` used
  deliberately to surface the optimistic-lock failure before the revision write.
- `FrameSpecifications.java` — JPA `Specification`/Criteria API, 7-field OR'd case-insensitive
  LIKE search, manual `%`/`_`/`\` escaping against LIKE-wildcard injection.
- `FrameImportService.java` — 186 lines, CSV upsert service (not yet read in full; page 6
  will read it in full during writing).

## Output structure

New folder: `PassTheSoftwareEngineeringInterview/topics/projects/global/`, mirroring
`topics/projects/arm-pythia/` exactly:

- `CLAUDE.md` — ground-truth rules file, same purpose as arm-pythia's: source locations table,
  a "verified facts" section (e.g. "MariaDB only, not MongoDB/OpenSearch — dropped
  deliberately, see ADR 0001", "CSV import actor string is literally `csv-import`, manual
  writes are `demo-user`", "no Kafka/Redis anywhere in the actual build"), and a document rule
  reminding future edits to verify against source before writing HTML.
- `INDEX.md` — lesson index table, same format as arm-pythia's (`| File | Concepts covered |`).
- 12 numbered HTML pages, `0001-*.html` through `0012-*.html`, same CSS/HTML skeleton already
  used in this project for `topics/projects/arm-pythia/*.html` (Georgia serif body, `.callout`
  boxes, `.q-card`/`story-card`-style content blocks as appropriate per page, no quiz
  click-to-reveal machinery needed here — see Non-goals). New pages should add a `.snippet`
  block style: a labeled code excerpt (file path + line range in a small monospace header)
  followed by an explanation paragraph — this is the primary content unit of every page.

## Page-by-page plan

Each page must contain **multiple real code excerpts** (copy-pasted from actual source, not
paraphrased/reconstructed), each followed by a short "what this does and why it's built this
way" explanation. Target: at least 3-4 labeled snippets per page for the code-heavy pages
(2-9), fewer but still present for the more discursive pages (10-12).

1. **Architecture Overview** — system map, layered modular monolith (`web`/`frame`/`config`/
   `observability`/`health` packages), actual vs. originally-suggested `docker-compose.yml`
   (side-by-side diff-style comparison: MongoDB/OpenSearch/OpenSearch-Dashboards present in
   the original, absent in the solution). Snippets: solution `docker-compose.yml` backend +
   mariadb-primary services (with healthcheck), package tree. **Diagram A** (SVG): component/
   layer diagram — controller → service → repository → MariaDB, frontend → backend via REST.

2. **Domain Model** — `Frame` entity fields vs. the legacy CSV's column groupings
   (identity/location/site & position/format/lifecycle/commercials from `INSTRUCTIONS.md` §4),
   what was kept/renamed/dropped and why. Snippets: `Frame.java` full field list + `create`/
   `apply`/`data()`, the `FrameData` record, `FrameStatus`/`FrameEnvironment`/`MediaType` enums.

3. **Frame CRUD & Optimistic Concurrency** — create/update flow, `@Version`, stale-write
   rejection. Snippets: `FrameCommandService.create()`, `.update()` (the version check +
   `ObjectOptimisticLockingFailureException` catch), `StaleFrameVersionException`. **Diagram B**
   (SVG): sequence diagram, two concurrent PUTs with the same version — one 200, one 409.

4. **Audit History** — diffing and revision recording. Snippets: `FrameData.changesFrom()`
   (not yet read — read during writing), `FrameRevision.record(...)` call sites in
   `FrameCommandService`, the no-op suppression (`if (changes.isEmpty()) return ...` without a
   revision write).

5. **Search** — JPA Specification pattern. Snippets: `FrameSpecifications.matching()` in full,
   the `escapeLike()` helper, the 7-field OR list, pagination controller wiring. Explanation of
   why leading-wildcard LIKE can't use an ordinary B-tree index, tied directly to ADR 0001.

6. **CSV Import** — `FrameImportService` (read in full during writing), header/shape validation
   before any write, per-row `REQUIRES_NEW` transaction via `upsertImportedFrame`, bounded row
   count (5,000) / size (2 MB) / returned errors (100), `FrameImportOutcome` classification.

7. **API Contracts, Error Handling & Observability** — `ApiExceptionHandler` (RFC 9457
   `ProblemDetail`), `RequestObservabilityFilter` (request-ID correlation, what gets logged vs.
   not — no bodies/query values), readiness health check wiring, container hardening notes
   (non-root user, loopback-only DB port) pulled from the README's "Operations and safeguards"
   section and verified against the actual Dockerfile/compose config during writing.

8. **Frontend Architecture** — React file layout (`api`/`hooks`/`pages`/`components`/`types`),
   design-system usage (tokens, provided components vs. custom), the stale-edit-recovery UX
   (409 → reload prompt) and partial-CSV-import result presentation, as concrete component
   snippets.

9. **Testing Strategy** — why integration-heavy initially (Testcontainers, cross-layer risk:
   MVC validation + transactions + JPA + Flyway + MariaDB semantics + optimistic locking +
   `ProblemDetail`), where unit tests were added instead (CSV import safeguards, frontend
   forms) and why, browser-automation scoped out under the one-day timebox. Snippets: one
   representative Testcontainers integration test, one focused unit test.

10. **What If: OpenSearch for Search** — concrete counterfactual grounded in the actual
    `FrameSpecifications` code from page 5: what code would be added/replaced, why a naive
    dual-write is wrong, an outbox-based sync sketch (frame write → outbox row → projector →
    OpenSearch document), and where the real justification threshold sits (fuzzy matching,
    geospatial queries, dataset size well past 1,000 rows) — directly extending ADR 0001's
    closing consequence.

11. **What If: Kafka & Event-Driven Patterns** — where Kafka would actually insert: (a) CSV
    import as an async job (upload → persisted job row → Kafka message → worker consumes in
    bounded batches → progress/retry state, extending ADR 0002), (b) revision fan-out to
    hypothetical downstream consumers (pricing, planning systems) via an outbox publisher after
    the same commit that already writes `FrameRevision`. Explicit callout on why a broker alone
    doesn't solve atomicity — the outbox pattern is the actual answer, matching ADR 0002's own
    line almost verbatim, expanded into a code-shaped sketch.

12. **Production Scale-Up Roadmap & AI-Collaboration Retrospective** — two closing sections in
    one page. First: consolidated gap list for real production/scale (auth — currently fixed
    demo actors; connection pool sizing under concurrent load; horizontal scaling readiness;
    async CSV + outbox + Kafka from pages 10-11; OpenSearch projection from page 10), framed as
    a prioritized "what would I do next" list, not a restatement of the README's limitations
    section. Second: honest account of the Entire/Codex checkpoint-capture failure (hooks not
    trusted during the session, zero checkpoints on `main`), using the implementation journal's
    gate-by-gate commit history (`91bdda9` through `1bca5c4`, with named review-fix commits like
    `43c9b39` and `714d2e4`) as the actual evidence of AI-collaboration discipline that exists
    in place of captured checkpoints.

## Visual style

Reuse the exact CSS skeleton already used for `topics/projects/arm-pythia/*.html` in this
session (Georgia serif, `:root` custom properties, `.callout`/`.callout.pass`/`.callout.warn`,
`lesson-nav` back-link). Add one new reusable block type: `.snippet` — a bordered monospace
code block with a small file-path/line-range label header, immediately followed by prose
explanation. Diagrams A and B are inline SVG, embedded directly in pages 1 and 3 respectively
(not separate numbered diagram pages, since this is one modular monolith, not a multi-service
platform — two targeted diagrams cover the genuinely diagram-worthy moments).

## Non-goals

- No quiz/click-to-reveal page (confirmed with user — this is self-study, not interview
  drilling).
- Not re-litigating or second-guessing the existing ADR decisions — expanding them with code,
  not proposing the user should have built it differently (the "what if" pages are explicitly
  counterfactual explorations, not implied criticism).
- Not a full diagram-per-topic set like arm-pythia's 4 dedicated diagram pages — 2 targeted
  diagrams, embedded inline.
- Not touching the actual `frame-inventory-exercise-main` solution repo — this is
  documentation only, written entirely under
  `PassTheSoftwareEngineeringInterview/topics/projects/global/`.

## Open items for writing time (not blocking spec approval)

- `FrameData.changesFrom()`, `FrameImportService.java` (186 lines), and the frontend
  stale-edit/partial-import components have not been read in full yet — will be read during
  the writing pass for pages 4, 6, and 8 respectively, per this project's own rule (verify
  against source before writing HTML, mirroring arm-pythia's `CLAUDE.md`).
