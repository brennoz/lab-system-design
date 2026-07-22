# PassTheSoftwareEngineeringInterview

Exam simulation lab. Claude picks questions, you answer, Claude reviews.  
Different from SystemDesignLab (deep build) — this is breadth + time pressure + feedback loop.

---

## Command Legend

| Command | Action |
|---|---|
| `next` | Claude picks question (30% algo / 20% system-design / 15% code-review / 10% ood / 8% java / 7% devops / 10% rest) |
| `next algo` | Force algorithms category |
| `next system-design` | Force system design category |
| `next code-review` | Force "spot the issue" — Claude shows legacy Java code, you find what's wrong |
| `next ood` | Force Object-Oriented Design. Delivers a full solution-walkthrough lesson + complete commented `solution/` + empty `answer/` — see "OOD Delivery Convention" below |
| `next ood skeleton` | Same as `next ood`, but `answer/` ships pre-seeded with partial/buggy code (planted issues) instead of empty — see "OOD Delivery Convention" below |
| `next spring` | Force Spring/SpringBoot/Hibernate category |
| `next messaging` | Force Kafka/Redis/RabbitMQ/WebSockets category |
| `next nosql` | Force MongoDB/Elasticsearch/Neo4j category |
| `next db` | Force databases/SQL category |
| `next concepts` | Force DDD/EDA/Microservices/AWS/Docker/K8s category |
| `next java` | Force Java / Java 8 features category |
| `next devops` | Force DevOps — rotates clusters (containers/kubernetes/helm/ci-cd/observability/alerting/terraform/agent-infra/architecture); picks least-recently-seen cluster from PROGRESS.md |
| `next devops containers` | Force containers cluster: Dockerfile, multi-stage builds, docker-compose, .dockerignore |
| `next devops kubernetes` | Force kubernetes cluster: Pods, Deployments, Services, Ingress, HPA, ConfigMap, Secrets, probes |
| `next devops helm` | Force helm cluster: chart structure, values.yaml, templating, hooks, rollback, canary |
| `next devops ci-cd` | Force ci-cd cluster: GitLab CI/CD stages, caching, GitOps, ArgoCD, registry push |
| `next devops observability` | Force observability cluster: Datadog APM, metrics, logs, traces, SLOs, dashboards |
| `next devops alerting` | Force alerting cluster: PagerDuty, Slack webhooks, alert rules, runbooks, on-call |
| `next devops terraform` | Force terraform cluster: providers, resources, modules, state, remote backend, workspaces |
| `next devops agent-infra` | Force agent-infra cluster: deploying AI agents on K8s, rate-limit sidecars, LLM observability, model version pinning |
| `next devops architecture` | Force full end-to-end DevOps architecture question — always hard; bypasses difficulty tracker |
| `next payments` | Force payment/fintech domain: card auth, idempotency, PCI-DSS, fraud detection, transaction logging, settlement flows |
| `next banking` | Force banking domain: double-entry ledger, KYC/AML, loan origination, SWIFT/SEPA, interest calculation, regulatory reporting |
| `next betting` | Force sport betting domain: odds feed, bet placement, liability management, settlement saga, responsible gambling, in-play latency |
| `next easy` | Force easy difficulty |
| `next medium` | Force medium difficulty |
| `next hard` | Force hard difficulty |
| `next {category} {slug}` | Force category + topic hint — e.g. `next ood bank`, `next algo sliding-window`. Scopes lesson folder and question topic. |
| `hint` | Claude gives a hint — saved to `hints.md` in question folder |
| `solution` | Claude shows full solution — saved to `hints.md` |
| `done` | Submit answer — Claude reviews + writes `review.md` |
| `skip` | Skip question, log as skipped in PROGRESS.md |
| `pause` | Save state to SESSION.md |
| `resume` | Restore from SESSION.md |
| `progress` | Show PROGRESS.md summary |
| `retry q{NNN}` | Retry a specific past question |

---

## Question Distribution

| Category | Probability | Technologies |
|---|---|---|
| Algo Coding | 30% | Data structures, sorting, graphs, trees, DP, JUnit, EDA code exercises; **domain-relevant:** LRU cache, sliding window rate limiter, token bucket, interval scheduling, graph traversal (BFS/DFS), heap-based top-K, stream processing, odds/payout calculation, ledger balance algorithms |
| System Design | 20% | Distributed systems, scalability, component design; **domain-relevant:** card auth flow, fraud detection, real-time odds feed, bet settlement saga, loan origination pipeline, ledger system, idempotent payment API, multi-region failover, settlement reconciliation |
| Code Review / Spot the Issue | 15% | Legacy Java code snippets — find design flaws, Spring anti-patterns, concurrency bugs, distributed system pitfalls, bad algorithms, security holes, JPA N+1, memory leaks, etc. |
| OO Design (coding) | 10% | Class diagrams → Java skeletons: SOLID, design patterns (Strategy, Observer, Factory, Builder, Decorator, Command, etc.), UML to code, parking lot, elevator, chess, ATM, vending machine |
| Java / Java 8 | 8% | Core Java, Java 8 (streams, lambdas, Optional, CompletableFuture, default methods, method refs), Java 11/17/21 features, collections, concurrency, JVM internals |
| DevOps | 7% | 9 clusters — containers (Dockerfile, multi-stage, compose), kubernetes (Pods/Deployments/HPA/Ingress/probes), helm (charts/templating/rollback), ci-cd (GitLab/ArgoCD/GitOps), observability (Datadog APM/SLO/dashboards), alerting (PagerDuty/Slack/runbooks), terraform (providers/modules/state), agent-infra (AI agents on K8s, LLM observability, model versioning), architecture (full pipeline end-to-end) |
| Rest | 10% | Spring/Boot/Hibernate/JPA, Spring Security, Kafka, Redis, RabbitMQ, PostgreSQL, Oracle, MongoDB, Elasticsearch, Neo4j, AWS, DDD, EDA, Microservices patterns, gRPC, REST design |

---

## Industry Domain Focus

Questions should rotate across three verticals. When relevant, mention that a topic maps to real industry interview patterns (without naming specific companies). Pick the domain that best fits the question — rotate across all three over time.

---

### Vertical 1 — Payment / Fintech

| Topic | What they probe |
|---|---|
| High-throughput systems | Millions of card transactions/sec — sharding, partitioning, async processing |
| PCI-DSS compliance | Card data (PAN) never in logs, encrypted at rest (AWS KMS), tokenisation |
| Idempotency | Duplicate requests must not double-charge — idempotency keys, DB conditional writes |
| Race conditions on balance | Concurrent debit on same account → overdraft — `@Version` / optimistic lock / DynamoDB conditional write |
| Fraud detection | Real-time anomaly detection, sliding window counters, feature stores, rule engines |
| Card authorization flow | Issuer → Network → Acquirer, timeout handling, fallback, reversals |
| Multi-region failover | Active-active vs active-passive, RTO/RPO, Route 53 health checks, Aurora Global |
| Rate limiting | Token bucket / sliding window per merchant/card, Redis atomics (INCR + EXPIRE) |
| Outbox pattern | Atomic DB write + event publish — prevents ghost payments on SNS/Kafka failure |
| Exactly-once semantics | Kafka idempotent producer + transactions, consumer deduplication |
| Legacy system migration | Strangler fig pattern, shadow mode validation, event-driven decoupling |
| Observability | Prometheus metrics, Splunk/CloudWatch logs, OpenTelemetry distributed tracing |
| mTLS / security | Service-to-service auth, secrets via AWS Secrets Manager, JWT/OAuth2 |
| Schema evolution | Avro + Schema Registry, backward/forward compatibility, no breaking changes |
| Canary / blue-green deploy | Feature flags, gradual rollout, instant rollback for payment services |

---

### Vertical 2 — Banking

| Topic | What they probe |
|---|---|
| Double-entry ledger | Every debit has a matching credit — immutable append-only ledger, no UPDATE on posted entries |
| Interest calculation | Daily accrual vs compound, `BigDecimal` precision (never `double` for money), end-of-day batch jobs |
| Loan origination pipeline | Credit score → underwriting → approval → disbursement as a saga; rollback on rejection |
| KYC / AML compliance | Identity verification pipeline, watchlist screening (OFAC/PEP), suspicious activity reports (SARs) |
| SWIFT / SEPA / ACH | Message formats (ISO 20022), settlement windows (T+1/T+2), retry on NACK, correspondent banking |
| Regulatory reporting | End-of-day reconciliation, Basel III capital ratios, batch aggregation, audit trail immutability |
| Core banking migration | Strangler fig + dual-write to old and new system; shadow mode validation before cutover |
| Overdraft / credit limit | Optimistic lock (`@Version`) + compensating transaction if limit exceeded post-commit |
| Data retention / GDPR | Right to erasure vs immutable audit trail conflict — pseudonymisation strategy |
| SOX compliance | No production access without audit log, separation of duties, 4-eyes approval for config changes |

**Code review triggers (always flag):**
- `double` or `float` for monetary amounts → use `BigDecimal`
- Mutable state in posted ledger entries → ledger rows must be append-only
- Missing audit log on any balance-changing operation
- Synchronous call to external credit bureau without timeout + circuit breaker

---

### Vertical 3 — Sport Betting

| Topic | What they probe |
|---|---|
| Real-time odds feed | WebSocket fan-out to millions of clients; Redis pub/sub for odds updates; back-pressure handling |
| Bet placement idempotency | Network retry → duplicate bet → double stake deduction — idempotency key per bet slip |
| Liability management | Max exposure per outcome tracked in Redis; suspend market when threshold hit; atomic INCR check |
| In-play low latency | Sub-100ms bet acceptance during live events; in-memory state + async DB write (write-behind cache) |
| Bet settlement | Outcome event triggers settlement saga: mark bets won/lost → credit winnings → publish receipt |
| Odds calculation | Decimal/fractional/American formats; overround (margin) ensures book profit; `BigDecimal` for payout |
| Race conditions on wallet | Concurrent bet placements deplete same wallet → overdraft — optimistic lock or Redis DECR atomic |
| Responsible gambling | Deposit limits, loss limits, self-exclusion (GamStop UK), cooling-off — enforced pre-bet, not post |
| Anti-fraud / bonus abuse | Matched betting detection (correlated accounts, IP/device fingerprint), velocity checks |
| Market suspension | Event triggers (red card, goal) → suspend all markets atomically → recalculate odds → reopen |
| KYC / AML | Mandatory in regulated markets (UK GC, MGA); verify identity before first withdrawal |
| High-traffic spikes | Super Bowl / World Cup — 100× normal load in minutes; auto-scaling, pre-warmed Lambda/ECS |
| Regulatory audit trail | Every bet, odds change, settlement immutably logged — WORM storage (S3 Object Lock) |

**Code review triggers (always flag):**
- `double`/`float` for odds or payout calculation → `BigDecimal`
- No idempotency key on bet placement endpoint → duplicate bets on retry
- Liability counter in application memory → wrong on multi-pod deployment → use Redis INCR
- Self-exclusion check missing or checked after wallet debit → regulatory violation
- No market suspension lock → bets accepted on stale odds during live event

---

## AWS / Distributed Context Rule

**For every question where applicable**, Claude must add an **"In AWS / Distributed Environment"** section covering:
- What AWS service(s) map to this component (e.g. SQS, ElastiCache, RDS Multi-AZ, Kinesis)
- What breaks at scale / on a multi-node cluster (e.g. in-memory state, local file system, single-instance assumptions)
- What the failure mode is in a distributed deployment (e.g. split-brain, duplicate processing, cache inconsistency)
- How AWS mitigates it (e.g. ElastiCache for shared cache, SQS FIFO for ordering, DynamoDB conditional writes for idempotency)

This applies to:
- Code review issues (add AWS impact to each issue's consequence)
- System design questions (AWS component mapping required)
- Concept questions (e.g. Kafka → MSK, Redis → ElastiCache, Postgres → RDS Multi-AZ)
- Algorithm questions where the algorithm solves a distributed problem (rate limiter, LRU cache)

---

## AI Engineering Extension Rule

**Applies to lessons only** (not `question.md`). Classic implementation always comes first. AI section always last, after the quiz, visually separated.

### When to add it
Add an AI Engineering Extension section to a lesson when the topic matches **any** of these triggers:

| Trigger | Example domains |
|---|---|
| Content moderation / spam detection | Comment system, social feed, chat |
| Fraud / anomaly detection | Payments, banking, betting wallet |
| Search / recommendation / ranking | Feed ranking, product search, autocomplete |
| Classification (intent, sentiment, category) | Support tickets, transaction tagging, bet type |
| Content generation / summarisation | News feed, document processing, reports |
| Document processing / extraction | KYC, loan origination, invoice parsing |
| Chatbot / conversational interface | Customer support, banking assistant |
| Personalisation / user behaviour | Feed ranking, notification timing, odds display |

### Section template (add to lesson HTML after quiz)

```html
<!-- AI Engineering Extension -->
<h2 style="border-color: #7c3aed">🤖 AI Engineering Extension</h2>

<h3>Where AI fits</h3>
<!-- which component benefits from an LLM -->

<h3>Pattern</h3>
<!-- RAG / classifier / agent / embedding search / tool-use / summarisation / extraction -->

<h3>Architecture delta</h3>
<!-- new boxes: vector DB, embedding service, LLM API call, prompt cache, guardrails -->

<h3>Tradeoffs</h3>
<!-- latency cost · hallucination risk vs rule-based · cost per call · accuracy · cold start -->

<h3>AWS / managed services</h3>
<!-- Bedrock · OpenSearch vector index · SageMaker · Kendra · Comprehend -->

<!-- AI Quiz — one question, labelled AI-Q, same format as main quiz -->
<div class="question" id="ai-q1">
  <p>AI-Q: ...</p>
  <ul class="options" data-correct="N"> ... </ul>
  <div class="explanation"> ... </div>
</div>
```

### DevOps AI Angle section template (add to devops lessons after quiz)

```html
<!-- DevOps Agentic AI Angle -->
<h2 style="border-color: #7c3aed">🤖 Agentic AI Angle</h2>

<h3>AI as workload</h3>
<!-- How this tool/config changes when the service being deployed IS an AI agent:
     GPU node pools, long-running inference pods, token-rate sidecars, model version pinning,
     prompt config via ConfigMap, cost-per-call metrics in Datadog -->

<h3>AI as operator</h3>
<!-- How AI agents USE or AUTOMATE this tool in the DevOps pipeline:
     Claude in GitLab CI as automated code reviewer, AI-powered incident triage,
     LLM generating Terraform plans from natural language, AI reading Datadog anomalies
     and opening Jira tickets automatically -->

<h3>Key differences from a standard service</h3>
<!-- GPU requirements, non-deterministic outputs (can't assert exact response in tests),
     model version = dependency (pin like a library version), token cost = runtime cost metric -->

<h3>AWS managed services</h3>
<!-- Bedrock (managed inference), SageMaker (custom model hosting),
     SSM Parameter Store (prompt versioning), Cost Explorer (token cost tracking) -->
```

### Section styling
Use a **purple left border** (`border-color: #7c3aed`) on the `h2` to visually distinguish from the main lesson content.

---

## Code Review Question Format

Claude writes a realistic Java snippet (20–60 lines) with **N planted issues** (N depends on difficulty: easy=2–3, medium=4–6, hard=7+). Issues span multiple categories intentionally — not all in one area.

Claude does **not** reveal N upfront. After `done`, Claude scores: issues found / total planted, per category.

Answer format in `MyAnswer.md`:
```
## Issues Found

### 1. [Category] — [short title]
Line X: [what is wrong]
Consequence: [what breaks in prod / at scale]
Fix: [what to change]

### 2. ...
```

---

## Difficulty Progression

Claude starts at **easy**. After 2 consecutive passes → bump to next level. On fail → hold level (or drop one if 2 consecutive fails). User can override with `next easy/medium/hard` at any time.

---
## OOD Delivery Convention

Applies to the `ood` category only — no other category changes delivery shape.

**Plain `next ood` (and `next ood {slug}`):**
1. Teach-first lesson generated before the question (existing `topics/ood/{slug}/INDEX.md`
   check applies as usual) — but the lesson itself is now a full solution-walkthrough doc:
   design reasoning, TDD narrative (red → green shown, not just described), and complete
   code for the primary recommended approach. Not a lighter pre-teaching primer.
2. `solution/` — complete, heavily commented (WHY, not what), TDD-ordered reference
   implementation matching what the lesson walks through. Always includes a runnable
   `{Entity}Main.java` demonstrating the solution end-to-end (construct the core objects,
   drive them through a realistic sequence of calls, print/assert observable outcomes) —
   same precedent as `q030_library_system/solution/LibraryMain.java`.
3. `answer/` — empty (a `.gitkeep` placeholder only). Write from scratch, or study
   `solution/` and the lesson first — the choice is yours, not enforced by withholding
   content.

**`next ood skeleton` (and `next ood skeleton {slug}`):**
1. Identical lesson + `solution/` as plain `next ood`.
2. `answer/` — pre-seeded with partial/buggy code (planted issues), same spirit as
   `q030_library_system/answer/attempt1`'s historical planted bugs (State pattern miswired,
   BigDecimal `==` reference-equality bug). Task: complete it and find what's wrong before
   `done`.

**Folder mirroring (both modes):** `solution/` and `answer/` are created under BOTH
`src/main/java/.../q{NNN}_{slug}/` AND `src/test/java/.../q{NNN}_{slug}/` — not just
`src/main`. Test code (the reference test suite in `solution/`, and wherever the learner's
own tests eventually land in `answer/`) mirrors the same `solution/`/`answer/` split as the
implementation code, exactly matching each package's directory layout.

**Both modes:** TDD is mandatory. JUnit 5 + Mockito only — no AssertJ (standing project-wide
rule, restated here since it's load-bearing for OOD specifically).

**Comment density (both modes, both `solution/` and its tests):** heavier than this
project's general default. Every non-trivial class or method in `solution/` explains, inline:
1. The design decision made (which pattern, which structure) and why it fits this specific
   requirement — not a restatement of what the code does.
2. At least one alternative that was considered and rejected, with the concrete reason
   (e.g. "enum + switch was rejected because adding a state means editing every switch
   block").
3. What NOT to do — the specific mistake this design guards against, ideally naming the
   actual bug shape (silent state corruption, a strategy holding elevator-specific state and
   losing reusability, a public setter bypassing a guard).

Test files get the same treatment, not just production code: comment on WHY a test is
structured the way it is, what it actually verifies vs. what a superficially-similar-but-
wrong version of the same test would miss. If a test was rewritten after an initial version
turned out not to test what it claimed, say so and explain why the fixed version is correct
— this project already caught real examples of that mid-session and it's worth keeping the
lesson attached to the code, not just in chat history.

This does not apply retroactively to already-built OOD questions (q013, q026, q027, q030,
q032, q035, q036, q037, q040) — forward-only from q042. The `{Entity}Main.java`
requirement specifically is forward-only from q044 — q042 and q043 predate it and were not
retrofitted.

---

## Question Workflow

1. User says `next` (or category/difficulty/slug variant)
2. Claude determines category + slug (e.g. `ood/bank`)
3. **Lesson check:** read `topics/{category}/{slug}/INDEX.md`
   - Concepts needed for the question **already listed** → skip to step 5
   - INDEX missing or concept **not listed** → announce *"No lesson for {slug} yet — generating before the question"*, then:
     a. Determine question spec (concept + difficulty) first
     b. Invoke `/teach` to generate exactly the lesson(s) needed
     c. Save to `topics/{category}/{slug}/0NNN-*.html`; update `INDEX.md`
4. Open lesson in browser
5. Generate full question file set on disk, post question in chat
6. Ideal time shown (informational — no hard cutoff)
7. User opens IDE, edits skeleton file
8. `hint` → Claude gives hint, appends to `hints.md`
9. `done` → Claude reads answer file, reviews against rubric, writes `review.md`
10. PROGRESS.md updated with result row

---

## Folder Structure Per Question

```
{module}/src/main/java/com/interview/{module}/q{NNN}_{slug}/
├── question.md      ← problem statement + constraints + skeleton description
├── Solution.java    ← skeleton (DO NOT EDIT — reference only)
└── MyAnswer.java    ← YOU write here

{module}/src/test/java/com/interview/{module}/q{NNN}_{slug}/
└── MyAnswerTest.java ← write tests here (TDD encouraged)

Concept/design questions:
system-design/q{NNN}_{slug}/
├── question.md
├── hints.md         ← populated on hint/solution requests
└── MyAnswer.md      ← you write here

concepts/q{NNN}_{slug}/
├── question.md
├── hints.md
└── MyAnswer.md

OO Design (coding skeletons):
ood/src/main/java/com/interview/ood/q{NNN}_{slug}/
├── question.md      ← problem + class diagram description
├── Solution.java    ← reference skeleton (interfaces + stubs)
└── MyAnswer.java    ← YOU implement here

devops/q{NNN}_{slug}/
├── question.md
├── hints.md
└── MyAnswer.md      ← or MyAnswer.yml / MyDockerfile as appropriate

Code review / spot the issue:
code-review/q{NNN}_{slug}/
├── question.md      ← contains the broken Java code snippet + "what is wrong?"
├── hints.md
└── MyAnswer.md      ← list every issue you find, categorised
```

**After `done`:** Claude creates `review.md` in the same folder.

---

## Review Rubrics

### Algorithms
- Correctness: does it produce right output?
- Time complexity: optimal or acceptable?
- Space complexity: any obvious waste?
- Edge cases: null, empty, duplicates, overflow handled?

### System Design
- Component selection: right tools for the job?
- Scalability reasoning: bottlenecks identified?
- Trade-offs articulated: consistency vs availability, etc.?
- Data model: storage decisions justified?
- **AWS mapping:** every component mapped to AWS equivalent (e.g. Kafka → MSK, Redis → ElastiCache, Postgres → RDS Multi-AZ, API Gateway → API GW + Lambda or ALB)
- **Distributed failure modes:** what breaks at 10x load, in a multi-AZ deployment, or during a region failover?
- **If payment domain:** idempotency addressed? PCI-DSS data handling? Multi-region RTO/RPO stated? Race conditions on balance? Outbox pattern for event publishing?

### Spring / Coding Exercises
- Correct patterns used (e.g. @Transactional, @Service boundaries)?
- Testable design (dependencies injectable)?
- Idiomatic Java 21?
- Spring Boot conventions followed?

### Code Review / Spot the Issue

Issues are scored by category. For each issue found:
- **Spring anti-patterns:** mutable state in `@Controller`/`@Service`, `@Autowired` field injection (not constructor), missing `@Transactional`, wrong propagation, lazy-load outside transaction
- **Distributed system pitfalls:** shared mutable state assumes single instance, in-memory cache not invalidated across nodes, no idempotency, missing retry/timeout, assuming local file system
- **Concurrency bugs:** non-thread-safe collections (`HashMap` in shared context), missing `synchronized`/`volatile`, double-checked locking without `volatile`, `SimpleDateFormat` shared instance
- **Algorithm / complexity:** O(N²) where O(N log N) possible, unnecessary DB query in loop (N+1), sorting unsorted input assumptions
- **JPA / Hibernate:** N+1 select, `EAGER` fetch on large collections, entity exposed directly as DTO, missing `@Version` for optimistic lock, transaction boundary too wide or too narrow
- **Security:** SQL string concatenation (injection), secrets in code, missing input validation, `@PreAuthorize` missing
- **Memory / resource leaks:** streams not closed, connection not released, large collection held in static field
- **Java style:** raw types, unchecked casts, empty catch blocks swallowing exceptions, `System.out.println` instead of logger

Strong answer: finds ALL planted issues, names the category, explains the real-world consequence, proposes fix, and where relevant explains **AWS/distributed impact** (e.g. "on a 4-pod ECS cluster this cache is per-instance → use ElastiCache Redis instead").

**Domain-specific code review triggers (always flag these):**

Payment / Fintech:
- Card/PAN data in logs → PCI-DSS violation, instant fail in compliance audit
- No idempotency key on payment mutation → double charge on retry
- No optimistic lock (`@Version`) on account balance → overdraft race condition
- `synchronized` on instance method when service runs as multiple replicas → false sense of safety
- Hardcoded AWS region or endpoint → breaks multi-region deployment
- Secrets/API keys in code or properties files → use AWS Secrets Manager / KMS
- No `@Transactional` on multi-step payment operations → partial failure = data inconsistency

Banking:
- `double`/`float` for monetary amounts → precision loss, regulatory non-compliance → use `BigDecimal`
- Mutable/UPDATE on posted ledger entries → audit trail destroyed → append-only ledger rows
- Missing audit log on balance-changing operation → SOX/regulatory violation
- No timeout + circuit breaker on external credit bureau call → cascading failure
- Loan/account state machine with no guard → invalid state transitions (e.g. approved → pending)

Sport Betting:
- `double`/`float` for odds or payout → wrong payouts, regulatory liability → use `BigDecimal`
- No idempotency key on bet placement → duplicate bets on client retry → duplicate stake deduction
- Liability counter in application memory → wrong on multi-pod → use Redis INCR (atomic)
- Self-exclusion / deposit limit check missing or checked after wallet debit → regulatory violation
- No market suspension during live event update → bets accepted on stale odds → financial loss

### OO Design
- SOLID principles applied?
- Correct pattern identified and named (e.g. Strategy, Observer)?
- Classes have single responsibility, low coupling?
- Skeleton compiles, interfaces/abstractions make sense?
- Extensibility: adding new behaviour doesn't require modifying existing classes?

### Java / Java 8
- Java 8 APIs used correctly (streams, Optional, CompletableFuture)?
- No unnecessary boxing/unboxing, no raw types?
- Thread safety addressed where relevant?
- Idiomatic use of lambdas and method references?

### DevOps / Docker / Kubernetes / GitLab / Terraform / Observability

**Hard gate — must pass:**
- **Prod awareness:** failure modes identified, not just happy path. Examples that must be caught:
  - containers: no .dockerignore → secrets in image layer; `latest` tag → non-reproducible deploys; running as root → container escape risk
  - kubernetes: missing resource limits → OOMKilled / noisy neighbour; missing probes → traffic routed to crashed pod
  - helm: no rollback strategy → failed deploy is stuck; hardcoded values in templates → not environment-portable
  - ci-cd: secrets hardcoded in `.gitlab-ci.yml` → exposed in CI logs; no cache → rebuild from scratch every run
  - observability: no SLO defined → alert fires but breach not tracked; missing distributed trace → can't pinpoint slow service
  - alerting: no runbook linked from alert → on-call engineer blind; PagerDuty without escalation policy → alert silently drops
  - terraform: state not in remote backend → corruption on team usage; no state locking → concurrent apply race condition
  - agent-infra: no rate-limit sidecar → LLM API costs unbounded; model version not pinned → silent behaviour change on redeploy

**Required for pass:**
- Architecture justification: WHY this config/tool choice, not just what it is

**Bonus (not required to pass):**
- Correct YAML/HCL/Dockerfile syntax
- Agentic AI angle mentioned
- AWS equivalent called out (EKS, ECR, MSK, ElastiCache, Secrets Manager, Bedrock)
- Cost / operational tradeoff named

**Difficulty scope:**
- Easy: single tool in isolation (Dockerfile, one K8s resource, one Terraform resource)
- Medium: two tools wired together (CI pipeline + Docker push; K8s Deployment + HPA + ConfigMap)
- Hard: full subsystem (K8s prod setup with Ingress + TLS + Secrets + HPA + probes; full CI/CD pipeline)
- Architecture (always hard): complete DevOps pipeline — git push → GitLab CI → Docker → ECR → Helm → ArgoCD → EKS → Datadog → Slack → Terraform underneath

### Messaging / NoSQL / DB
- Technology chosen correctly for the use case?
- Configuration/schema correct?
- Failure scenarios considered?

### Concepts / DDD / EDA
- Domain language precise?
- Boundaries correctly identified?
- Patterns named and applied correctly?

---

## Lesson Pipeline (`/teach` skill)

`/teach` skill is the **only** lesson system. `teach me` is retired.

### Output path override
When `/teach` runs in this project, save lessons to `topics/{category}/{slug}/` — **not** `./lessons/`. Numbering is per-topic: `0001-*.html` within each slug folder restarts at 0001, not global.

### Topic folder structure
```
topics/
  ood/{slug}/          e.g. bank, parking-lot, elevator, atm
  algo/{slug}/         e.g. two-sum, sliding-window, bfs
  system-design/{slug}/
  java/{slug}/
  spring/{slug}/
  messaging/{slug}/
  nosql/{slug}/
  db/{slug}/
  devops/{slug}/
  concepts/{slug}/
```

### INDEX.md — concept registry per topic
Every topic folder contains `INDEX.md`. Format:

```markdown
# {slug} — Lesson Index

| File | Concepts covered |
|---|---|
| 0001-*.html | Template Method, BigDecimal, synchronized |
| 0002-*.html | Strategy Pattern, AccountStatus enum |
```

Claude reads this before deciding whether to generate a new lesson. If the concept needed for the next question is already listed, skip lesson generation.

### Teach-first decision tree
```
next {category} {slug}
  │
  ├─ topics/{category}/{slug}/INDEX.md exists
  │   AND concepts for this question already listed?
  │     YES → skip lesson generation
  │     NO  → announce "No lesson for {slug} yet — generating before the question"
  │           → determine question spec first (concept + difficulty)
  │           → invoke /teach for exactly the lesson(s) needed
  │           → save to topics/{category}/{slug}/0NNN-*.html
  │           → append entry to INDEX.md
  │
  └─ generate full question file set
```

### question.md lesson link
Every generated `question.md` includes a `Lessons:` line at the top linking to the HTML lesson(s) that cover its concepts:
```
**Lessons:** [0001 Domain Model](../../../../../topics/ood/bank/0001-domain-model.html)
```

### Aid distinction

| Aid | When to use |
|---|---|
| `/teach` (auto) | Fires before first question on a new topic — zero knowledge assumed |
| `hint` | Stuck mid-exercise — some knowledge, need nudge |
| `solution` | Gave up |

---

## Study Flow (before retry)

Before retrying a question, read:
- `hints.md` — every hint/solution given during original attempt
- `review.md` — rubric feedback from Claude

---

## Maven Modules

| Module | Run tests |
|---|---|
| algorithms | `mvn test -pl algorithms` |
| spring-questions | `mvn test -pl spring-questions` |
| messaging | `mvn test -pl messaging` |
| nosql | `mvn test -pl nosql` |
| databases | `mvn test -pl databases` |
| ood | `mvn test -pl ood` |
| interview-prep | `mvn test -pl interview-prep` |
| All | `mvn test` |

---

## Testing Stack

**New tests: JUnit 5 + Mockito only — no AssertJ.** `assertj-core` stays as a pom dependency for existing tests that already use it (not retroactively rewritten), but every test written from here on uses plain JUnit 5 (`org.junit.jupiter.api.Assertions`) and Mockito, not AssertJ's fluent `assertThat`.

- `BigDecimal` comparisons: `assertEquals(0, actual.compareTo(expected))` — never `assertEquals(expected, actual)` directly, since `BigDecimal.equals()` is scale-sensitive (`100.00` != `100.0`).
- Exceptions: `assertThrows(SomeException.class, () -> ...)`.
- Timeouts/deadlock checks: `assertTimeoutPreemptively(Duration.ofSeconds(N), () -> ...)`.
