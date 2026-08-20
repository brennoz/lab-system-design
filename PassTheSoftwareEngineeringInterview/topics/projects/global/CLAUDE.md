# Global Frame Inventory Study Docs — CLAUDE.md

Ground-truth rules derived from reading actual source code in the user's solution repo.
**Before editing any HTML page here, verify claims against source. Never assume.**

---

## Source Locations

| What | Path |
|---|---|
| Original exercise skeleton (as provided by Global) | `/home/brennoz77/LABS/LAB_INTERVIEWS/original/frame-inventory-exercise-main/` |
| The user's solution (what was actually built) | `/home/brennoz77/LABS/LAB_INTERVIEWS/frame-inventory-exercise-main/` |
| Solution repo (public) | `https://github.com/brennoz/frame-inventory-exercise-main` |
| Backend source | `frame-inventory-exercise-main/src/backend/src/main/java/com/global/ct/frameinventory/` |
| Frontend source | `frame-inventory-exercise-main/src/frontend/src/` |
| Existing ADRs (source of truth for "why") | `frame-inventory-exercise-main/docs/adr/0001-relational-persistence-and-sql-search.md`, `0002-synchronous-transactional-csv-import.md`, `0003-optimistic-concurrency-and-transactional-audit-history.md` |
| Implementation journal (gate-by-gate delivery record) | `frame-inventory-exercise-main/docs/implementation-journal.md` |
| Conversation record | `frame-inventory-exercise-main/docs/conversation-record.md` |
| Solution README | `frame-inventory-exercise-main/README.md` |

---

## Verified Facts

- **Datastore: MariaDB only.** The original suggested `docker-compose.yml` offered MongoDB,
  MariaDB, and OpenSearch + Dashboards as options. The solution kept MariaDB only and removed
  MongoDB, OpenSearch, OpenSearch Dashboards, Kafka, and Redis entirely. There is no Kafka or
  Redis anywhere in the actual build — any mention of them in pages 0010/0011 is explicitly a
  counterfactual ("what if"), not a description of what was built.
- **Frame IDs are immutable.** `Frame.frameId` is `@Column(..., updatable = false)`. Create
  rejects a duplicate ID (`DuplicateFrameException`); update never changes it.
- **Optimistic locking via JPA `@Version`** (a `long` column named `version`). Clients must
  send back the version they read; a stale write throws
  `ObjectOptimisticLockingFailureException`, caught and rethrown as
  `StaleFrameVersionException` → HTTP `409 Conflict`.
- **Audit actors are fixed strings**, not real identities (no auth is implemented): manual
  writes use `"demo-user"`, CSV-imported writes use `"csv-import"`. Both are literal constants
  in `FrameCommandService`.
- **CSV import bounds:** max 5,000 data rows, 2 MB file size, at most 100 detailed row errors
  returned (total failed count is still accurate even when truncated). Each row is upserted in
  its own `@Transactional(propagation = REQUIRES_NEW)` call — one bad row never rolls back a
  good one, and the import is deliberately **not** parallelized across virtual threads (ADR
  0002 explains why: 1,000-row files don't justify the added nondeterminism).
  CSV parsing uses Apache Commons CSV (`CSVFormat.RFC4180`), header-name mapping, duplicate
  headers disallowed.
- **Revisions only exist for actual changes.** `Frame.update()` computes a diff first
  (`FrameData.changesFrom()`); if the diff is empty, no revision row is written, even though
  the request was technically an "update" call.
- **Search is SQL LIKE-based**, not full-text or fuzzy. `FrameSpecifications.matching()` builds
  a JPA `Specification` that OR's a case-insensitive LIKE across 7 fields (frameId, siteNumber,
  station, address, town, postcode, format), with manual escaping of `%`, `_`, and `\` in the
  user's query to prevent LIKE-wildcard injection. Status/environment/mediaType are exact-match
  filters, not text search.
- **Entire (the required checkpoint-capture CLI) failed to capture anything.** Zero checkpoints
  exist on `main`; the Codex hooks were never trusted during the actual working session. This
  is a real, documented gap — write about it honestly in page 0012, not glossed over. The
  `implementation-journal.md` is the reconstructed substitute record, built from git history and
  visible conversation milestones, not a verbatim session transcript.
- **Testing:** backend tests use Testcontainers against a real MariaDB (not H2/mocks) for the
  highest-risk cross-layer behavior; focused unit tests were added later for CSV import
  safeguards and frontend forms. No browser-automation suite — out of scope for the one-day
  timebox (this is stated directly in the README's "Current limitations" section).

## Document Rules

### Snippet accuracy
Every `.snippet` block's code must be copy-pasted from the actual file, not reconstructed from
memory or from this CLAUDE.md's prose. Re-read the source file with the Read tool immediately
before writing a snippet and use its real current line numbers in the label.

### No Kafka/OpenSearch/Redis claims outside pages 0010-0011
Pages 0001-0009 and 0012 describe what was actually built. Do not describe OpenSearch, Kafka,
Redis, or MongoDB as being used anywhere in those pages — they were removed. Pages 0010 and
0011 are explicitly counterfactual and should be clearly framed as "if we had chosen X instead."

### Actor strings
Always write the literal strings `demo-user` and `csv-import` (in code font) when referring to
audit actors — never invent a different placeholder name.
