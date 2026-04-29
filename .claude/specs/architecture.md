# Architecture Reference — DDD + EDA Patterns

## Domain-Driven Design (DDD)

### Strategic Patterns
| Pattern | Description | When to use |
|---|---|---|
| **Bounded Context** | Explicit boundary around a domain model | One per microservice |
| **Ubiquitous Language** | Shared vocabulary between devs and domain | Define in each project CLAUDE.md |
| **Context Map** | How bounded contexts relate (ACL, Shared Kernel, etc.) | Document before coding |
| **Anti-Corruption Layer (ACL)** | Translate between contexts | When consuming external APIs |

### Tactical Patterns
| Pattern | Description | Java hint |
|---|---|---|
| **Entity** | Has identity, mutable state | Class with `@Id`, equals by ID |
| **Value Object** | Immutable, equals by value | Java `record` |
| **Aggregate** | Consistency boundary, has one root | Root entity controls invariants |
| **Domain Event** | Something that happened in the domain | Past tense record, immutable |
| **Repository** | Collection abstraction for aggregates | Interface in domain, impl in infra |
| **Domain Service** | Logic that doesn't belong to one entity | Stateless, named as verb |
| **Application Service** | Orchestrates use cases | Calls domain, handles transactions |

---

## Event-Driven Architecture (EDA)

### Core Patterns
| Pattern | Problem solved | Implementation |
|---|---|---|
| **Outbox Pattern** | Lost events when broker is down | Persist event to DB table in same TX; poll and publish |
| **Saga (Choreography)** | Distributed transactions without 2PC | Each service listens for event, emits next | 
| **Saga (Orchestration)** | Centralized saga state management | Orchestrator service drives the flow |
| **CQRS** | Read/write model separation | Write model (commands) ≠ read model (queries) |
| **Event Sourcing** | State as sequence of events | Replay events to rebuild aggregate state |
| **Dead Letter Queue (DLQ)** | Failed message handling | Route poison messages to DLQ topic |

### Saga — Choreography vs Orchestration
```
Choreography (decentralised):
  ServiceA → emits EventA → ServiceB reacts → emits EventB → ServiceC reacts
  Pro: loose coupling. Con: hard to visualise flow.

Orchestration (centralised):
  Orchestrator → calls ServiceA → awaits reply → calls ServiceB → ...
  Pro: explicit flow. Con: orchestrator becomes coupling point.
```

### Outbox Pattern Flow
```
1. Application TX: INSERT into domain_table + INSERT into outbox_events (same TX)
2. Outbox poller (Debezium or scheduled): reads unpublished outbox rows
3. Publishes to Kafka/RabbitMQ
4. Marks outbox row as published
Guarantees: at-least-once delivery. Make consumers idempotent.
```

---

## Infrastructure Decisions

| Need | Tool | Notes |
|---|---|---|
| Event streaming (high throughput) | **Kafka** | Topics: `<project>.<context>.<event>` |
| Task queues (work distribution) | **RabbitMQ** | Exchanges + queues per bounded context |
| Cache / distributed state | **Redis** | Cache-Aside pattern; TTL always set |
| Relational data | **PostgreSQL** | One schema per bounded context |
| Object storage | **LocalStack (S3)** | `aws --endpoint-url=http://localhost:4566` |
| API simulation | **WireMock** | Simulate external airline APIs, payment gateways |
| Service resilience | **resilience4j** | Circuit Breaker, Rate Limiter, Retry, Bulkhead |
| API Gateway | **Spring Cloud Gateway** | Single entry point, routing, auth filter |

---

## Algorithms Reference

| Algorithm | Used in | Complexity |
|---|---|---|
| Base62 encoding | TinyURL | O(log N) space |
| Consistent Hashing | Key-Value Store, cache sharding | O(log N) lookup |
| Token Bucket | Rate Limiter | O(1) per request |
| Sliding Window Counter | Rate Limiter | O(1) with Redis sorted sets |
| Merge Sort (merge phase) | Flight result ranking | O(N log N) |
| Dijkstra / A* | Google Maps routing | O((V+E) log V) |
| QuadTree | Google Maps spatial indexing | O(log N) lookup |
| Bloom Filter | Key-Value Store (key existence) | O(k) lookup, false positives only |
| LSM Tree | Key-Value Store storage engine | O(log N) write, O(log N) read |
| Fan-out on Write | Twitter timeline | O(followers) on write |
| Fan-out on Read | Twitter timeline (celebrities) | O(followers) on read |
