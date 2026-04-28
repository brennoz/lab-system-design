# System Design Lab — Root Conventions

## Purpose
Multi-project study lab covering classic system design problems via DDD + EDA patterns.
Each project is a standalone microservices system under `SystemDesignLab/`.

---

## Stack (ALL projects)

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3+, Maven multi-module |
| Frontend | React 18, TypeScript, Vite |
| Event streaming | Apache Kafka |
| Task queues | RabbitMQ |
| Cache | Redis |
| Databases | PostgreSQL (relational), MongoDB (document, when needed) |
| Object storage | LocalStack (AWS S3 simulation) |
| API simulation | WireMock |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5, Mockito, Testcontainers, AssertJ |
| API docs | SpringDoc OpenAPI |

---

## Java Conventions

- Java 21 — use records, sealed classes, pattern matching where they clarify intent
- Maven multi-module: one parent `pom.xml` per project, one module per bounded context service
- Run tests: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test`
- Package structure: `com.lab.<project>.<service>.<layer>` (e.g. `com.lab.flight.search.domain`)
- Layers per service: `domain`, `application`, `infrastructure`, `api`
- No Lombok — use Java records for DTOs and value objects

---

## TDD Rules

1. Write the failing test FIRST — never write implementation before a test
2. Run tests to confirm they fail (red)
3. Write minimum code to make them pass (green)
4. Refactor if needed, keep tests green
5. Use Testcontainers for integration tests (real Postgres, real Redis, real Kafka)
6. Use Mockito for unit tests — mock at the boundary (repositories, external clients)

---

## Comment Rules (MANDATORY)

Every non-trivial class or method MUST have a one-line comment referencing:
- **Pattern:** the design/architectural pattern applied
- **Algorithm:** if an algorithm is used, name it + complexity
- **Why:** non-obvious constraint, trade-off, or invariant

Format:
```java
// Pattern: Outbox — atomically persists event with domain state, prevents lost events
// Algorithm: Token Bucket — O(1) amortised per request via Redis atomic INCR + TTL
// Why: idempotency key prevents double-charge on retry
```

Do NOT add comments that just restate the method name. Only add when WHY is non-obvious.

---

## Naming Conventions

- Services: `<bounded-context>-service` (e.g. `search-service`, `booking-service`)
- Domain events: past tense (e.g. `FlightSearched`, `BookingConfirmed`, `PaymentFailed`)
- Commands: imperative (e.g. `SearchFlightsCommand`, `ConfirmBookingCommand`)
- REST endpoints: plural nouns (`/flights`, `/bookings`, `/users`)
- Kafka topics: `<project>.<context>.<event>` (e.g. `flight.booking.confirmed`)

---

## Git / GitHub

- Monorepo: `lab-system-design` — one repo, one folder per project
- Commit messages: terse, imperative (e.g. `add Flight aggregate with TDD`)
- One commit per meaningful unit of work (don't batch unrelated changes)
- Branch per project feature if needed

---

## Docker Compose

- Each project has its own `docker-compose.yml`
- Services start with `docker-compose up -d` from the project root
- Health checks on all infra containers before app containers start
- Use named volumes for data persistence

---

## Claude Behavior in This Lab

- Default mode: **auto** — Claude implements fully unless user says "step by step"
- After each service is complete: run code review subagent (`/review`)
- No trailing summaries in responses — state result and next step only
- Terse responses; use tables and code blocks, not prose paragraphs

---

## Projects

| # | Folder | Status |
|---|--------|--------|
| 01 | `01-flight-aggregator` | 🔨 In Progress |
| 02 | `02-bank-system` | ⏳ Pending |
| 03 | `03-rate-limiter` | ⏳ Pending |
| 04 | `04-tiny-url` | ⏳ Pending |
| 05 | `05-twitter` | ⏳ Pending |
| 06 | `06-discord` | ⏳ Pending |
| 07 | `07-youtube` | ⏳ Pending |
| 08 | `08-google-drive` | ⏳ Pending |
| 09 | `09-google-maps` | ⏳ Pending |
| 10 | `10-key-value-store` | ⏳ Pending |
| 11 | `11-library-system` | ⏳ Pending |
| 12 | `12-distributed-message-queue` | ⏳ Pending |
