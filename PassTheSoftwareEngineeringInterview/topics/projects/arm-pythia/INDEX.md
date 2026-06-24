# arm-pythia — Lesson Index

| File | Concepts covered |
|---|---|
| 0001-architecture-overview.html | System map, bounded contexts, Kafka vs RabbitMQ vs Redis WHY, AWS topology |
| 0002-feed-handlers.html | Feed Handler pattern, lib-feed-handlers Template Method, 18 integrations, Kafka bridge |
| 0003-event-service.html | PostgreSQL schema, Quartz scheduler, Kafka topic ownership, REST controllers, Hexagonal arch |
| 0004-bet-manager-service.html | Bet placement lifecycle, Spring Retry, Saga choreography, idempotency, multi-tenancy |
| 0005-pricing-service.html | Redis caching strategy, TTL design, odds calculation, consumer group partitioning |
| 0006-sportsbook-service.html | STOMP hub, RabbitMQ relay, Redis fan-out, Kafka 5-topic fan-in, WebSocket config |
| 0007-bet-lifecycle.html | End-to-end bet flow: placement → advisory → settlement → STOMP push |
| 0008-kafka-topics.html | Full topic table, WOODS vs PYTHIA clusters, consumer group IDs, at-least-once delivery |
| 0009-websocket-stomp.html | STOMP protocol, RabbitMQ relay port 61613, SockJS fallback chain, frontend auth |
| 0010-infrastructure.html | EKS clusters, MSK, Redis cluster, Helm HPA, GitLab CI/CD stages, ECR, Terraform |
| 0011-ood-patterns.html | Hexagonal arch, Feed Handler Template Method, Saga, CQRS, lib-tenants multi-tenancy |
| 0012-sdlc-security-observability.html | Architecture decisions, coding standards, testing strategy, deployment, JWT security, Datadog→Slack, Danger→Slack |
| 0013-quiz-master.html | 25-question comprehensive quiz, all topics, click-to-reveal |
