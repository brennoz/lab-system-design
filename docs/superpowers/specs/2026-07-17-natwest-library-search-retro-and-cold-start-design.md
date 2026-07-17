# NatWest Library Search Retro + Cold-Start TDD Practice — Design

## Context

3rd (live coding) NatWest interview: given a real take-home repo (`resources/natwest/`) —
a `Library` class with a `search(String)` stub that ignores its argument and returns the
whole catalog unfiltered, plus a `LibraryTest` expecting exact-match, case-insensitive, and
title-OR-author substring search. Full toolkit on the classpath (commons-lang3, JUnit5,
AssertJ, Hamcrest, Mockito, Lombok) — a signal that library usage (e.g.
`StringUtils.containsIgnoreCase`) was expected, not incidental.

Candidate froze: blanked on the filter implementation, overthought design scope (plain
stream filter vs introducing a pattern), under time pressure. Prior prep in this project
(`ood/q030_library_system`, `interview-prep/.../ex4_library_borrowing`) had already built a
much richer model (BookCopy, State pattern, audit trail, Member) — a calibration gap: the
richer model wasn't wasted effort, it's where this exact catalog naturally evolves once more
tickets land, but that bridge wasn't visible live. Ticket delivery was very likely
progressive (search ticket, then more added once each passed) though this wasn't confirmed
before time ran out.

Two goals: (1) retro this specific exercise so the gap is closed, (2) build a repeatable
practice mode so the freeze/overthink pattern doesn't recur on the next similar take-home.

## Part 1 — Retro: `ood/q041_library_search`

New question in the existing `ood/` module, following project conventions
(`ood/src/main/java/com/interview/ood/q041_library_search/`).

**Lesson first (teach-first rule applies):** `topics/ood/library-system/INDEX.md` doesn't yet
cover search/filtering — 0001 only covers the State-pattern/reservation model from q030. Add:

- `topics/ood/library-system/0002-case-insensitive-search.html` — covers `Stream.filter`,
  `StringUtils.containsIgnoreCase` vs manual `toLowerCase().contains()`, why classpath-visible
  libraries are a usage hint, OR-across-fields predicate composition.
  Ends with an **Evolution path** section, built for repetition (multiple reinforcement
  angles per stage — paragraph + code diff + quiz question — since retention across
  repeated exposure is the explicit goal, not a single read-through):

  | Stage | Ticket that forces it | Why `List<Book>` breaks | What replaces it |
  |---|---|---|---|
  | 1 | Search title/author (q041) | nothing breaks yet | `Stream.filter` over `List<Book>` |
  | 2 | Borrow a book | one `Book` = one title, but N physical copies exist | `Book` (work) vs `BookCopy` (physical, has status), 1:N |
  | 3 | Return a book | boolean `borrowed` flag allows illegal double-return/double-borrow | `CopyStatus` enum + guarded transitions |
  | 4 | Reserve when all copies out | scattered if/else guards become unreadable as states grow | State pattern — each status owns its legal transitions |
  | 5 | Track who borrowed what | no record survives a status flip back to available | `AuditTrail` / `Member` — append-only history |

  Stages 4–5 explicitly point at the existing `q030_library_system` (State pattern) and
  `ex4_library_borrowing` (audit/exceptions) solutions as the answer key — "you already
  built this, in a different exercise" — repetition across artifacts, not just within the page.
  Append entry to `INDEX.md`.

**Question package (no `answer/attempt1` — nothing salvageable from the live attempt):**

```
ood/src/main/java/com/interview/ood/q041_library_search/
├── question.md       ← Lessons: link to 0002; ticket verbatim; given stub shown;
│                        retro note block: bug diagnosis, calibration-gap lesson,
│                        test-reading checklist (read full test file first, treat
│                        given tests as spec not adversary, run-and-watch-fail before
│                        implementing)
└── solution/
    ├── Book.java      ← copy of given Book, untouched
    └── Library.java   ← fixed: StringUtils.containsIgnoreCase OR across title/author

ood/src/test/java/com/interview/ood/q041_library_search/
└── solution/SolutionTest.java   ← literal given LibraryTest, unchanged
```

PROGRESS.md gets a new row: `q041 | library_search | ood | 2026-07-17 | — | — |
⚠️ Retro→Fixed | ...` (same retrospective-tag style already used for q030).

## Part 2 — `next cold`: cold-start TDD practice mode

New opt-in command, separate from the normal random `next` distribution (doesn't touch the
existing 30/20/15/10/8/7/10 category math). Only fires on explicit `next cold`.

**Rules, deliberately different from normal `next`:**
- No `/teach` lesson generated beforehand — real interviews don't warm you up
- No `hint` available before `done` — forces working through the freeze rather than
  escaping it
- Delivered as bare stub + given tests + pom + one-line ticket text only — no question.md
  prose explaining intent
- Progressive: a repo ships with multiple tickets. First `next cold` on a repo reveals
  ticket 1 only. Passing it (`done`) reveals ticket 2 in the same repo, and so on — mirrors
  the escalating-ticket format this NatWest round used.

**Folder structure (new top-level, sibling to `ood/`, `algorithms/`, etc. — not the
question.md/Solution.java/MyAnswer.java convention, since no prose/hints are exposed):**

```
cold-start/
├── PROTOCOL.md                     ← anti-panic / test-reading checklist, read BEFORE a
│                                      session starts (never mid-session): tests are the
│                                      spec; run and watch it fail before writing code;
│                                      ship the simplest thing that passes first, no pattern
│                                      until a ticket explicitly demands extensibility;
│                                      if a given test looks wrong, assume it's right unless
│                                      it contradicts the ticket, ask once, move on
├── seed-bank/
│   ├── 001_library_catalog/        ← this NatWest repo's shape, reused for repetition
│   │   ├── pom.xml
│   │   ├── src/main/java/...       ← stub only
│   │   ├── src/test/java/...       ← ticket 1's test only, revealed with ticket 1
│   │   ├── tickets/01-search.md, 02-borrow.md, 03-return.md   ← revealed progressively
│   │   └── reference-solution/     ← Claude-only, never shown to user during a session;
│   │                                  used to review `done` submissions
│   ├── 002_transaction_filter/     ← payments vertical variant
│   └── 003_market_suspension/      ← betting vertical variant
└── attempts/{date}_{slug}/         ← live attempt copy, kept for retro after `done`
```

**Content sourcing:** hybrid. Seed bank starts with 001 (this exercise's shape) plus 2–3
more Claude-authored progressive-ticket repos across the project's existing payment/betting
verticals. Once the seed bank is exhausted, `next cold` falls back to generating a fresh
repo on demand using the same authoring rules (stub + tests + progressive tickets +
hidden reference solution). PROGRESS.md tracks which seed-bank repos and generated repos
have been used, per ticket, so repeats aren't served back-to-back.

**PROGRESS.md tracking:** cold-start sessions get their own rows, ticket-granular (similar
to how multi-issue code-review rows are logged), tagged category `cold-start`.

## CLAUDE.md changes

- Add `next cold` to the Command Legend table in
  `PassTheSoftwareEngineeringInterview/CLAUDE.md`, with the no-lesson/no-hint/progressive-
  ticket rules noted inline.
- No change to the category-distribution table — cold-start is explicitly outside it.

## Out of scope

- Does not fold `next cold` into the random `next` roll (kept opt-in per decision above).
- Does not rebuild `ex4_library_borrowing` or `q030_library_system` — they're referenced as
  the evolution answer key, not modified.
- Does not attempt to reconstruct tickets 2/3 of the actual NatWest exercise (never
  confirmed) — `cold-start/seed-bank/001_library_catalog` tickets 2/3 are original,
  plausible continuations (borrow/return), not a claimed reproduction of the real follow-up.
