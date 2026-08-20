# Global Frame Inventory — Lesson Index

| File | Concepts covered |
|---|---|
| 0001-architecture-overview.html | System map, layered modular monolith, actual vs. originally-suggested docker-compose stack, component/layer diagram |
| 0002-domain-model.html | Frame entity, FrameData value object, enums, legacy CSV column mapping decisions |
| 0003-frame-crud-optimistic-concurrency.html | Create/update flow, JPA @Version, StaleFrameVersionException, concurrent-conflict sequence diagram |
| 0004-audit-history.html | FrameData.changesFrom() diffing, FrameRevision/FrameRevisionChange, same-transaction coupling, no-op suppression |
| 0005-search.html | JPA Specification pattern, LIKE-wildcard escaping, pagination, B-tree index limitation |
| 0006-csv-import.html | FrameImportService, validate-before-write, per-row REQUIRES_NEW transactions, partial-success reporting |
| 0007-api-error-handling-observability.html | ApiExceptionHandler / RFC 9457 ProblemDetail, RequestObservabilityFilter, readiness health check, container hardening |
| 0008-frontend-architecture.html | React file layout, design-system usage, stale-edit recovery UX, partial-import result presentation |
| 0009-testing-strategy.html | Testcontainers integration tests, focused unit tests, browser-automation scoping decision |
| 0010-what-if-opensearch.html | Counterfactual: OpenSearch for search, outbox-based sync, when the switch would be justified |
| 0011-what-if-kafka.html | Counterfactual: Kafka for async CSV import and revision fan-out, outbox pattern, atomicity |
| 0012-production-roadmap-and-ai-retrospective.html | Production scale-up gap list, honest Entire/Codex checkpoint-capture retrospective |
