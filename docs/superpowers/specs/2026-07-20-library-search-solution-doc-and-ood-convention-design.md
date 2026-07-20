# Library Search Solution Doc + Standing OOD Convention — Design

## Context

Supersedes `docs/superpowers/specs/2026-07-17-natwest-library-search-retro-and-cold-start-design.md`
(that design, and its implementation plan, are abandoned — no code from that plan was ever
written; only the design/plan docs themselves exist on disk, left as historical record).

Same root cause as before: 3rd NatWest interview, given a `Library.search(String)` stub that
ignores its argument, froze on implementation + scope uncertainty + a calibration gap
against richer library models already built in this project (`q030_library_system`,
`ex4_library_borrowing`). This time the ask is narrower and more concrete: a deep
solution-walkthrough doc covering multiple ways to implement the search, built as the actual
`q041_library_search` question — plus a standing change to how all future OOD questions get
delivered in this project.

## Part 1 — `ood/q041_library_search`

Follows the existing `ood/` module conventions (`question.md` + `solution/` + `answer/`),
now upgraded per Part 2's new default.

**`solution/`** — one compiling, fully-tested implementation using the **Specification
pattern**, not a plain inline-lambda filter:
- `Book.java` — the given class, untouched
- `BookSpecification.java` — interface: `boolean isSatisfiedBy(Book book)`
- `TitleContainsSpecification.java`, `AuthorContainsSpecification.java` — concrete criteria
- `OrSpecification.java` — combinator
- `Library.java` — `search()` composes `TitleContainsSpecification` OR
  `AuthorContainsSpecification` and filters the catalogue against it
- Heavily commented throughout (WHY, not what — per this project's comment convention)

**`solution` test** — `SolutionTest.java` is the literal given `LibraryTest` (exact match,
case-insensitive, title-OR-author substring) — same contract as the real take-home, proving
the Specification-based design is behaviorally identical to the naive fix, just structured
differently.

**TDD flow (documented in the lesson, not just performed):** red (given buggy stub + given
test, run and watch it fail) → green (Specification-based fix, run and watch it pass) →
explain why Specification over inline lambda in the write-up.

**`answer/`** — stays empty. This is the default `next ood` mode (see Part 2) — the real
live attempt produced nothing salvageable, so there's no `answer/attempt1` historical record
here (unlike q030).

**`question.md`** — ticket verbatim, given code shown (`Book`, `Library` stub, `LibraryTest`,
pom dependency list), the retro note (bug, why the freeze happened, test-reading checklist)
carried over from the prior design's Part 1 content, and a `Lessons:` link to the new lesson
below.

**PROGRESS.md** — new row, same retrospective-tag style as `q030`'s partial→fixed entry.

## Part 2 — Lesson: `topics/ood/library-system/0002-search-strategies.html`

Shown **before** the question is attempted (existing teach-first pipeline), but — per the
new standing convention in Part 3 — this is no longer a light concept primer. It's a full
solution-walkthrough doc: design reasoning, TDD narrative, and complete code for the layers
it covers in depth.

**Structure:**

1. **Baseline** — plain `Stream.filter` over title/author, case-insensitive substring. The
   minimal correct fix to the given bug. Why it's limited: any new search criterion means
   editing `Library` itself, violating Open/Closed.
2. **Specification pattern** (matches what ships in `solution/`, full code shown) —
   `BookSpecification` interface, `TitleContainsSpecification` /
   `AuthorContainsSpecification`, `OrSpecification` combinator. New criteria are new classes;
   `Library` and existing specifications stay untouched. This is presented as the
   recommended answer for the actual interview scope.
3. **Relevance-ranked search** (sketched in the doc, not compiled/tested code) — score each
   match (exact title match > starts-with > contains-only), sort descending by score instead
   of a binary include/exclude filter. Discusses when this matters (catalogue search UX)
   and the complexity/latency cost of scoring every candidate.
4. **Index-based search at scale** (sketched, explicitly system-design-flavored per your
   standing request for a broader-than-OOD section even when tangential) — inverted index
   (token → set of book IDs) or trie for prefix search, for when linear scan over the
   catalogue stops being viable. Cross-references this project's existing AWS/Distributed
   Context Rule: maps to OpenSearch/Elasticsearch, notes what breaks at scale (in-memory
   `List<Book>` on a single instance) and the distributed failure mode (stale index across
   replicas without a shared search service).
5. **Integration with existing library work** — how the Specification pattern composes
   forward once `BookCopy` (from `q030_library_system`) enters the picture: an
   `AvailableSpecification` (checks `CopyStatus`) would combine with the existing title/author
   specifications via `AndSpecification` without touching search logic at all — sketched in
   the lesson only, not a compiled class in `solution/` (which only needs `Or`, to satisfy
   the actual given test's title-OR-author contract). Explicit bridge
   to `q030_library_system` and `ex4_library_borrowing` as where this catalogue naturally
   evolves, same bridging intent as the prior (abandoned) design's evolution-path section,
   now folded into this lesson instead of a separate one.

Retrieval quiz at the end, same click-to-reveal script/CSS convention as `0001-library-domain-model.html`.

`topics/ood/library-system/INDEX.md` gets a new row for 0002.

## Part 3 — Standing convention change: OOD question delivery

Applies to the `ood` category only (not other categories). Update
`PassTheSoftwareEngineeringInterview/CLAUDE.md`'s Question Workflow / Command Legend
section:

**Plain `next ood` (and `next ood {slug}`):**
1. Teach-first lesson generated before the question, now as a full solution-walkthrough doc
   (design reasoning + TDD narrative + complete code for the primary recommended approach,
   per Part 2's shape) — not a lighter pre-teaching primer.
2. `solution/` — complete, heavily commented, TDD-ordered reference implementation.
3. `answer/` — empty. Write from scratch, or study `solution/` and the lesson first — your
   call.

**`next ood skeleton` (and `next ood skeleton {slug}`):**
1. Identical lesson + `solution/` as above.
2. `answer/` — pre-seeded with partial/buggy code (planted issues), same spirit as
   `q030_library_system/answer/attempt1`'s historical planted bugs (State pattern miswired,
   BigDecimal `==`). Task: complete it and find what's wrong.

**Both modes:** TDD is mandatory, JUnit 5 + Mockito only — no AssertJ (already a standing
project-wide rule, restated here since it's load-bearing for OOD specifically).

## Out of scope

- Does not touch `q030_library_system` or `ex4_library_borrowing` — referenced as the
  integration/evolution answer key, not modified.
- Does not build the `cold-start`/`next cold` practice mode from the abandoned design —
  dropped entirely, not carried forward.
- Layers 3 (ranked) and 4 (indexed) are documentation-only for q041 — no compiling Java
  files, per the explicit scope decision to keep `solution/` to one recommended
  implementation.
- Does not retroactively rebuild older OOD questions (q013, q026, q027, q032, q035, q036,
  q037, q040) under the new convention — applies going forward only.
