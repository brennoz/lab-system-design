# library-system — Lesson Index

| File | Concepts covered |
|---|---|
| [0001-library-domain-model.html](0001-library-domain-model.html) | State Pattern (BookCopy lifecycle: AVAILABLE→RESERVED→BORROWED→OVERDUE→LOST), Strategy Pattern (FineCalculationStrategy: daily vs flat rate), core entity design (Book vs BookCopy), LibraryService orchestration, BigDecimal for monetary values, borrow limit enforcement, common-mistake callouts (State pattern self-mutation instead of delegation, BigDecimal `==` reference-equality bug) pulled from real q030 grading history |
| [0002-search-strategies.html](0002-search-strategies.html) | Specification Pattern (BookSpecification + Title/Author/Or), case-insensitive substring search via `StringUtils.containsIgnoreCase`, reading classpath-visible-but-unused libraries as an implementation hint, relevance-ranked search (scored + sorted), inverted-index search at scale (AWS OpenSearch mapping, distributed in-memory-index failure mode), integration bridge to q030's BookCopy/State Pattern model — full solution-walkthrough retro of a real NatWest take-home |
