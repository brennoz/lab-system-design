# DevOps + Agentic AI Section — Design Spec

**Project:** PassTheSoftwareEngineeringInterview  
**Date:** 2026-07-09  
**Status:** Approved

---

## Decision Summary

| Decision | Choice |
|---|---|
| Structure | One unified `devops` section — no split |
| Answer format | Concept-only `MyAnswer.md` |
| Lesson HTML | Existing structure + AI angle always at bottom |
| Review hard gate | Prod awareness |
| Syntax correctness | Optional / bonus |
| `next devops` routing | Rotate least-recently-seen cluster; slug override |
| Architecture slug | Always hard; not affected by difficulty tracker |
| Agentic AI split | `agent-infra` = AI as workload; `ci-cd`+`observability` = AI as operator |

---

## Section Structure

### Folder layout

```
devops/
  q{NNN}_{slug}/
    question.md       ← problem statement + what to cover
    hints.md          ← populated on hint/solution
    MyAnswer.md       ← user writes here (concept explanation, no YAML required)
    review.md         ← written by Claude after `done`

topics/devops/
  {slug}/
    INDEX.md          ← concept registry (same pattern as ood/algo)
    0001-*.html       ← lesson HTML (teach-first, always before question)
```

---

## Clusters

| Slug | Focus | Agentic AI angle |
|---|---|---|
| `containers` | Dockerfile (multi-stage), docker-compose, image layering, .dockerignore, build cache | AI as workload: containerising an LLM inference service |
| `kubernetes` | Pods, Deployments, Services, Ingress, HPA, ConfigMap, Secrets, resource limits, probes | GPU node pools, long-running agent pods, token-rate sidecar |
| `helm` | Chart structure, values.yaml, templating, release management, hooks, rollback | Helm chart for LLM service; canary rollout of model versions |
| `ci-cd` | GitLab CI/CD stages, caching, matrix builds, GitOps (ArgoCD), registry push | Claude in CI as automated code reviewer; AI-generated release notes |
| `observability` | Datadog APM, metrics, logs, traces, dashboards, SLOs | Token usage metrics, LLM latency tracking, cost anomaly alerts |
| `alerting` | PagerDuty, Slack webhooks, alert rules, runbooks, on-call rotation | AI-powered incident triage: LLM reads Datadog alert → generates runbook action |
| `terraform` | Provider config, resources, modules, state, plan/apply, remote backend, workspaces | Provisioning GPU node groups; managing LLM API keys in Secrets Manager via TF |
| `agent-infra` | Deploying AI agents on K8s: rate-limit sidecars, prompt config via ConfigMap, model version pinning, cost guardrails, observability for LLM calls | Core cluster — all content is AI-as-workload |
| `architecture` | Full end-to-end DevOps pipeline for a Spring Boot microservice — all clusters wired together | AI agents as both workload and operator in the same architecture |

---

## Anchor Scenario

All questions and lessons are grounded in this system:

```
git push
  → GitLab CI (build + test + Docker image → ECR)
  → Helm chart updated (values.yaml image tag bumped)
  → ArgoCD detects GitOps drift → deploys to EKS
  → Kubernetes: Deployment + HPA + ConfigMap + Secrets + Ingress + TLS
  → Datadog: APM traces + structured logs + SLO dashboard + cost metrics
  → Slack/PagerDuty: alert fires on SLO breach → AI triage → runbook
  → Terraform: provisioned EKS cluster + RDS + MSK + ECR + Secrets Manager
  → AI agent layer: LLM sidecar on K8s, traced via Datadog, deployed via Helm, canary via ArgoCD
```

Service: **Spring Boot microservice** (consistent with rest of project).

---

## Lesson HTML Structure

Same CSS/format as existing lessons (`topics/ood/bank/`, `topics/algo/`), with these sections:

1. **Concept** — what this tool is and where it fits in the pipeline
2. **Architecture diagram** (ASCII) — tool's place in the anchor scenario
3. **Annotated snippets** — YAML/HCL/Dockerfile with inline comments on every non-obvious line
4. **What breaks without this** — concrete failure mode if this config is missing or wrong
5. **Prod pitfalls** — real failure modes (OOMKilled, ImagePullBackOff, Helm hook ordering, etc.)
6. **AWS mapping** — equivalent managed service (EKS, ECR, Secrets Manager, MSK, etc.)
7. **Quiz** — 2–3 interactive questions (same format as OOD/algo lessons)
8. **🤖 Agentic AI angle** ← always at the bottom, purple left border (`border-color: #7c3aed`)

### AI angle section template (devops variant)

```html
<h2 style="border-color: #7c3aed">🤖 Agentic AI Angle</h2>

<h3>AI as workload</h3>
<!-- how this tool changes when the service being deployed IS an AI agent -->

<h3>AI as operator</h3>
<!-- how AI agents can USE or AUTOMATE this tool in the DevOps pipeline -->

<h3>Key differences from a standard service</h3>
<!-- GPU requirements, long-running pods, token rate limits, model version pinning, cost per call -->

<h3>AWS managed services</h3>
<!-- Bedrock for inference, SageMaker for hosting, SSM Parameter Store for prompt versioning -->
```

---

## `next devops` Routing

```
next devops
  → read PROGRESS.md → find least-recently-seen cluster
  → announce cluster + slug
  → teach-first check (topics/devops/{slug}/INDEX.md)
  → generate lesson if concept not covered
  → generate question

next devops {slug}        → force that cluster (e.g. next devops kubernetes)
next devops architecture  → always hard; full end-to-end pipeline question
next devops easy/medium/hard  → force difficulty within rotation-selected cluster
next devops {slug} easy   → force both
```

Rotation order tracked in PROGRESS.md via cluster column. Architecture slug bypasses difficulty tracker — always hard.

---

## Difficulty Levels

| Level | Scope | Example |
|---|---|---|
| Easy | Single tool in isolation | Write and explain a multi-stage Dockerfile for a Spring Boot app |
| Medium | Two tools wired together | GitLab CI pipeline that builds, tests, pushes Docker image to ECR |
| Hard | Full subsystem | K8s production setup: Deployment + HPA + Ingress + TLS + ConfigMap + Secrets + liveness/readiness probes |
| Architecture | Always hard — full pipeline | Design the complete DevOps infrastructure for the anchor Spring Boot service |

---

## Review Rubric

### Hard gate (must pass to score a pass)

**Prod awareness:** failure modes identified, not just happy path. Examples:
- K8s: missing resource limits → OOMKilled / noisy neighbour
- Helm: no rollback strategy → failed deploy is stuck
- Datadog: no SLO defined → alert fires but no SLO breach tracked
- Terraform: state not in remote backend → state corruption on team usage
- CI/CD: secrets hardcoded in `.gitlab-ci.yml` → exposed in logs
- agent-infra: no rate-limit sidecar → LLM API costs unbounded

### Required (needed for pass)

**Architecture justification:** WHY this config/tool choice, not just what it is.

### Optional / bonus

- Correct YAML/HCL syntax
- Agentic AI angle mentioned
- AWS equivalent called out
- Cost/operational tradeoff named

---

## CLAUDE.md Changes Required

1. Add `next devops {slug}` to Command Legend
2. Expand DevOps row in Question Distribution table to reference clusters
3. Add DevOps Review Rubric section
4. Add Lesson Pipeline override: `topics/devops/{slug}/`
5. Add AI angle section template (devops variant)
6. Update `next devops` description: "Force DevOps — rotates clusters (containers/kubernetes/helm/ci-cd/observability/alerting/terraform/agent-infra/architecture)"
