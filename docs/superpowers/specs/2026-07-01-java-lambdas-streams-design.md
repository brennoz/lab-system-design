# Java Lambdas & Streams — Lesson Design

**Date:** 2026-07-01  
**Output:** `PassTheSoftwareEngineeringInterview/topics/concepts/java/`

---

## Goal

Two HTML lesson files teaching Java lambdas and streams by contrast with imperative code. Interview-focused. Domain examples rotate across payment, banking, sport-betting verticals.

---

## Files

| File | Title | Concepts |
|---|---|---|
| `0002-lambdas.html` | Java Lambdas & Functional Interfaces | Anonymous inner classes → lambda syntax → functional interfaces → method references → function composition → closures |
| `0003-streams.html` | Java Streams — Pipeline & Collectors | Lazy pipeline concept → creation → intermediate ops → terminal ops → collectors → Optional → primitive streams → parallel |

---

## Structure per file

Same style as `0001-completable-future.html`:
- Georgia serif body, monospace code blocks, callout boxes, quiz with JS reveal
- Nav link back to `../../`
- Meta line: Category / Topic / Est. time / Lesson N of N

---

## File 1 — `0002-lambdas.html`

### Sections
1. **Before lambdas** — anonymous inner class verbosity; `Comparator`, `Runnable`, `ActionListener` examples; counts LOC overhead
2. **Lambda syntax** — `(params) -> expr` vs `(params) -> { block }` ; type inference; single param parens optional
3. **Functional interfaces** — `@FunctionalInterface`; built-in: `Predicate<T>`, `Function<T,R>`, `Consumer<T>`, `Supplier<T>`, `BiFunction<T,U,R>`, `UnaryOperator<T>`, `BinaryOperator<T>`; each with imperative vs lambda comparison
4. **Method references** — 4 types: `ClassName::staticMethod`, `instance::method`, `ClassName::instanceMethod`, `ClassName::new`; when to prefer over lambda
5. **Function composition** — `andThen`, `compose`, `Predicate.and/or/negate`; building pipelines without nesting
6. **Closures & effectively final** — why captured vars must be effectively final; mutation alternatives (`AtomicReference`, extract method)
7. **Quiz** — 5 questions

### Domain examples
- `Predicate<Transaction>` filtering high-value payments
- `Function<Account, BigDecimal>` computing interest
- `Comparator` sorting bet slips by stake

---

## File 2 — `0003-streams.html`

### Sections
1. **What a Stream is** — lazy evaluation, not a data store; pipeline = source + intermediates + terminal; `for` loop vs stream mental model
2. **Creating streams** — `Collection.stream()`, `Stream.of()`, `Arrays.stream()`, `Stream.generate()`, `Stream.iterate()`, `IntStream.range()`
3. **Intermediate ops** — `filter`, `map`, `flatMap`, `distinct`, `sorted`, `limit`, `skip`, `peek`; each with before/after imperative
4. **Terminal ops** — `forEach`, `collect`, `reduce`, `count`, `min/max`, `findFirst/findAny`, `anyMatch/allMatch/noneMatch`
5. **Collectors deep-dive** — `toList()`, `toUnmodifiableList()`, `toMap()`, `groupingBy()`, `partitioningBy()`, `joining()`, `counting()`, `summingInt/averagingInt`, `summarizingInt`
6. **Optional** — replacing null returns; `of/ofNullable/empty`, `map`, `flatMap`, `orElse/orElseGet/orElseThrow`, `filter`, `ifPresent`; anti-patterns (`isPresent()` + `get()`)
7. **Primitive streams** — `IntStream`, `LongStream`, `DoubleStream`; boxing cost; `mapToInt/mapToLong/mapToDouble`; `sum/average/range`
8. **Parallel streams** — `parallelStream()`; when to use (CPU-bound, large data, no shared state); when NOT to (I/O, ordered output needed, small lists, shared mutable state)
9. **Quiz** — 6 questions

### Domain examples
- `groupingBy` grouping bets by market type
- `partitioningBy` splitting transactions into approved/declined
- `reduce` summing account balances in a ledger
- `Optional` wrapping nullable account lookup

---

## Constraints

- No Testcontainers, no Spring context — pure Java examples
- All code blocks compile against Java 21 (but lambdas/streams are Java 8+; note where Java 9+ or 16+ applies)
- `data-correct="N"` verified before saving (N = 1-based index)
- INDEX.md updated after both files written
