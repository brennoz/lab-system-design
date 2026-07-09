# DevOps + Agentic AI Section Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a fully-functional `devops` section to PassTheSoftwareEngineeringInterview with 9 topic clusters, teach-first HTML lessons, concept-only answers, prod-awareness rubric, and Agentic AI angles throughout.

**Architecture:** CLAUDE.md is updated to define routing, rubric and lesson rules. The first cluster (`containers`) gets a full lesson HTML and a seed question so the section is immediately usable. The remaining 8 clusters generate on demand via the existing teach-first workflow when `next devops {slug}` is called.

**Tech Stack:** Markdown, HTML/CSS/JS (same lesson template as `topics/ood/`), no new dependencies.

**Spec:** `docs/superpowers/specs/2026-07-09-devops-agentic-ai-section-design.md`

---

## Task 1: Update CLAUDE.md — Commands

**Files:**
- Modify: `PassTheSoftwareEngineeringInterview/CLAUDE.md:23`

- [ ] **Step 1.1: Replace the single `next devops` line with the full cluster routing**

Find this line (line ~23):
```
| `next devops` | Force GitLab CI/CD / Docker / Kubernetes / DevOps category |
```

Replace with:
```
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
```

- [ ] **Step 1.2: Verify command table looks correct — no duplicate pipes, aligned columns**

---

## Task 2: Update CLAUDE.md — Question Distribution

**Files:**
- Modify: `PassTheSoftwareEngineeringInterview/CLAUDE.md:51`

- [ ] **Step 2.1: Replace the DevOps distribution row**

Find (line ~51):
```
| DevOps | 7% | GitLab CI/CD pipelines, Docker (Dockerfile, multi-stage, compose), Kubernetes (pods, deployments, services, ingress, HPA, ConfigMap, secrets), Helm, container networking |
```

Replace with:
```
| DevOps | 7% | 9 clusters — containers (Dockerfile, multi-stage, compose), kubernetes (Pods/Deployments/HPA/Ingress/probes), helm (charts/templating/rollback), ci-cd (GitLab/ArgoCD/GitOps), observability (Datadog APM/SLO/dashboards), alerting (PagerDuty/Slack/runbooks), terraform (providers/modules/state), agent-infra (AI agents on K8s, LLM observability, model versioning), architecture (full pipeline end-to-end) |
```

---

## Task 3: Update CLAUDE.md — DevOps Review Rubric

**Files:**
- Modify: `PassTheSoftwareEngineeringInterview/CLAUDE.md:367-372`

- [ ] **Step 3.1: Replace the DevOps rubric section**

Find (lines ~367-372):
```
### DevOps / Docker / Kubernetes / GitLab
- Dockerfile stages correct (multi-stage for Java)?
- K8s resource types chosen correctly (Deployment vs StatefulSet, etc.)?
- GitLab CI stages logical (build → test → package → deploy)?
- Secrets not hardcoded; ConfigMap vs Secret distinction clear?
- Health checks / liveness + readiness probes mentioned?
```

Replace with:
```
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
```

---

## Task 4: Update CLAUDE.md — Lesson Pipeline AI Angle Template for DevOps

**Files:**
- Modify: `PassTheSoftwareEngineeringInterview/CLAUDE.md` (AI Engineering Extension section)

- [ ] **Step 4.1: Add devops AI angle template after the existing AI Engineering Extension section**

Find the line:
```
<!-- AI Quiz — one question, labelled AI-Q, same format as main quiz -->
```

After the full AI Engineering Extension section block, add:

```markdown
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
```

---

## Task 5: Scaffold DevOps Folder Structure

**Files:**
- Create: `devops/` (question root)
- Create: `topics/devops/containers/INDEX.md`

- [ ] **Step 5.1: Create the devops question root and topics structure**

```bash
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/devops
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/containers
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/kubernetes
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/helm
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/ci-cd
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/observability
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/alerting
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/terraform
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/agent-infra
mkdir -p /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/topics/devops/architecture
```

- [ ] **Step 5.2: Create `topics/devops/containers/INDEX.md`**

```markdown
# containers — Lesson Index

| File | Concepts covered |
|---|---|
| [0001-dockerfile-multistage.html](0001-dockerfile-multistage.html) | Multi-stage Dockerfile, build cache layering, .dockerignore, non-root user, image size, prod pitfalls (latest tag, root user, no .dockerignore), AWS ECR, AI angle (containerising LLM inference service) |
```

---

## Task 6: Create Containers Lesson HTML

**Files:**
- Create: `topics/devops/containers/0001-dockerfile-multistage.html`

- [ ] **Step 6.1: Create the full lesson HTML**

Create `topics/devops/containers/0001-dockerfile-multistage.html` with this content:

```html
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Containers — Multi-Stage Dockerfile for Spring Boot</title>
<style>
  :root {
    --ink: #1a1a1a; --muted: #555; --border: #d0d0d0;
    --accent: #1a56db; --bg-soft: #f8f8f6; --bg-note: #eff6ff;
    --bg-warn: #fffbeb;
  }
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: Georgia, 'Times New Roman', serif;
    font-size: 17px; line-height: 1.75; color: var(--ink);
    max-width: 860px; margin: 0 auto; padding: 3rem 2rem 6rem;
  }
  h1 { font-size: 2rem; line-height: 1.2; margin-bottom: 0.4rem; }
  h2 { font-size: 1.25rem; margin: 2.5rem 0 0.8rem; padding-left: 0.8rem; border-left: 4px solid var(--accent); }
  h3 { font-size: 1rem; margin: 1.4rem 0 0.4rem; font-weight: bold; }
  p  { margin: 0.7rem 0; }
  ul, ol { padding-left: 1.6rem; margin: 0.6rem 0; }
  li { margin: 0.3rem 0; }
  code { font-family: 'Menlo','Consolas',monospace; font-size: 0.83em; background: #f0f0f0; padding: 0.1em 0.35em; border-radius: 3px; }
  pre {
    font-family: 'Menlo','Consolas',monospace; font-size: 0.8em;
    background: #1e1e2e; color: #cdd6f4;
    padding: 1rem 1.2rem; border-radius: 6px; overflow-x: auto;
    margin: 0.8rem 0; line-height: 1.5;
  }
  pre .c  { color: #6c7086; font-style: italic; }
  pre .kw { color: #cba6f7; }
  pre .st { color: #a6e3a1; }
  pre .nu { color: #f9e2af; }
  table { width: 100%; border-collapse: collapse; margin: 1rem 0; font-size: 0.88rem; }
  th { background: var(--bg-soft); text-align: left; padding: 0.5rem 0.8rem; border: 1px solid var(--border); }
  td { padding: 0.45rem 0.8rem; border: 1px solid var(--border); vertical-align: top; }
  tr:nth-child(even) td { background: var(--bg-soft); }
  .meta { color: var(--muted); font-size: 0.88rem; margin-bottom: 2rem; }
  .callout { background: var(--bg-note); border-left: 4px solid var(--accent); padding: 0.9rem 1.1rem; margin: 1.2rem 0; border-radius: 0 4px 4px 0; }
  .callout-warn { background: var(--bg-warn); border-left-color: #d97706; }
  .callout-red  { background: #fef2f2; border-left-color: #dc2626; }
  .callout-green{ background: #f0fdf4; border-left-color: #16a34a; }
  hr { border: none; border-top: 1px solid var(--border); margin: 2.5rem 0; }
  .pipeline { font-family: 'Menlo','Consolas',monospace; font-size: 0.78em; background: #1e1e2e; color: #cdd6f4; padding: 1.2rem 1.4rem; border-radius: 6px; margin: 1rem 0; line-height: 2; white-space: pre; }
  .quiz-section { margin-top: 3rem; padding-top: 1.5rem; border-top: 2px solid var(--border); }
  .question { background: var(--bg-soft); border: 1px solid var(--border); border-radius: 6px; padding: 1rem 1.2rem; margin-bottom: 1.2rem; }
  .question p { margin-bottom: 0.6rem; font-weight: bold; }
  .options { list-style: none; padding: 0; }
  .options li { padding: 0.45rem 0.9rem; margin: 0.3rem 0; border: 1px solid var(--border); border-radius: 4px; cursor: pointer; transition: background 0.15s; }
  .options li:hover { background: #e0e7ff; }
  .options li.correct { background: #dcfce7; border-color: #16a34a; }
  .options li.wrong   { background: #fef2f2; border-color: #dc2626; }
  .explanation { display: none; margin-top: 0.8rem; padding: 0.7rem 0.9rem; background: #f0fdf4; border-left: 3px solid #16a34a; border-radius: 0 4px 4px 0; font-size: 0.9rem; }
</style>
</head>
<body>

<h1>Containers — Multi-Stage Dockerfile for Spring Boot</h1>
<p class="meta">Cluster: containers · Tools: Docker, ECR · Difficulty: Easy → Medium</p>

<hr>

<h2>Where containers fit in the pipeline</h2>

<div class="pipeline">git push
  → GitLab CI
      → <strong>docker build (multi-stage)</strong>   ← YOU ARE HERE
      → docker push → ECR
  → Helm chart updated (image tag)
  → ArgoCD → EKS
  → Datadog traces + logs
  → Terraform provisioned ECR + EKS underneath</div>

<p>Docker packages your Spring Boot JAR and its runtime into a portable, immutable image. Every environment (dev, staging, prod) runs the <em>exact same image</em> — no "works on my machine".</p>

<hr>

<h2>Why multi-stage?</h2>
<p>A naive Dockerfile copies your entire Maven build environment (JDK, Maven cache, source code) into the final image. That produces a ~600 MB image with the full JDK and build tools — attack surface you don't need at runtime.</p>

<p>Multi-stage: use one stage to build, a second stage to run. Only the compiled JAR crosses the boundary.</p>

<table>
  <thead><tr><th>Approach</th><th>Image size</th><th>Contains build tools?</th><th>Security surface</th></tr></thead>
  <tbody>
    <tr><td>Single stage (JDK)</td><td>~600 MB</td><td>Yes</td><td>Large — JDK, Maven, source</td></tr>
    <tr><td>Multi-stage (JDK build → JRE run)</td><td>~200 MB</td><td>No</td><td>Small — JRE + JAR only</td></tr>
    <tr><td>Multi-stage + distroless</td><td>~120 MB</td><td>No</td><td>Minimal — no shell, no package manager</td></tr>
  </tbody>
</table>

<hr>

<h2>Annotated Dockerfile</h2>

<pre>
<span class="c"># ── Stage 1: BUILD ───────────────────────────────────────────────────────────</span>
<span class="c"># Use the full JDK image only in this stage — it never reaches prod.</span>
<span class="kw">FROM</span> eclipse-temurin:<span class="nu">21-jdk-alpine</span> <span class="kw">AS</span> build

<span class="kw">WORKDIR</span> /app

<span class="c"># Copy pom.xml FIRST, then download dependencies — separate layer.</span>
<span class="c"># Why? Docker caches layers. If pom.xml hasn't changed, this layer hits cache</span>
<span class="c"># and Maven download is skipped on the next build. Saves 2-3 min per CI run.</span>
<span class="kw">COPY</span> pom.xml .
<span class="kw">RUN</span> mvn dependency:go-offline -q

<span class="c"># Copy source AFTER dependencies — source changes don't bust the dep cache.</span>
<span class="kw">COPY</span> src ./src

<span class="c"># -DskipTests: tests already ran in an earlier CI stage. Don't run twice.</span>
<span class="kw">RUN</span> mvn package -DskipTests -q

<span class="c"># ── Stage 2: RUN ─────────────────────────────────────────────────────────────</span>
<span class="c"># JRE only — no compiler, no Maven, no source code.</span>
<span class="kw">FROM</span> eclipse-temurin:<span class="nu">21-jre-alpine</span>

<span class="kw">WORKDIR</span> /app

<span class="c"># Never run as root inside a container.</span>
<span class="c"># If an attacker escapes the app, root in container = root on host (without seccomp).</span>
<span class="c"># Create a dedicated user with no shell and no home directory.</span>
<span class="kw">RUN</span> addgroup -S appgroup &amp;&amp; adduser -S appuser -G appgroup
<span class="kw">USER</span> appuser

<span class="c"># Copy only the JAR from the build stage — nothing else crosses the boundary.</span>
<span class="kw">COPY</span> --from=build /app/target/*.jar app.jar

<span class="c"># Document the port — doesn't publish it (that's docker run -p or K8s Service).</span>
<span class="kw">EXPOSE</span> <span class="nu">8080</span>

<span class="c"># exec form: signals (SIGTERM) go directly to the JVM, not to a shell wrapper.</span>
<span class="c"># Shell form (CMD "java -jar ...") means kill sends SIGTERM to sh, not Java —</span>
<span class="c"># graceful shutdown never fires, K8s forcefully kills the pod after terminationGracePeriodSeconds.</span>
<span class="kw">CMD</span> [<span class="st">"java"</span>, <span class="st">"-jar"</span>, <span class="st">"app.jar"</span>]
</pre>

<hr>

<h2>.dockerignore</h2>
<p>Without <code>.dockerignore</code>, the <code>COPY . .</code> instruction sends your entire working directory to the Docker daemon — including <code>.git/</code>, <code>target/</code>, local <code>.env</code> files with secrets, and node_modules if present. This:</p>
<ul>
  <li>Slows every build (large build context transferred)</li>
  <li>Busts the layer cache when <code>.git</code> changes (every commit)</li>
  <li>Risks leaking secrets into image layers</li>
</ul>

<pre>
<span class="c"># .dockerignore</span>
target/
.git/
.env
*.env
.idea/
*.iml
**/*.log
node_modules/
</pre>

<hr>

<h2>What breaks without this</h2>

<div class="callout-red callout">
  <strong>Using <code>latest</code> tag:</strong> <code>docker pull myapp:latest</code> gives you whatever was pushed last — different image on each pull. In K8s, <code>imagePullPolicy: IfNotPresent</code> with <code>latest</code> means pods on different nodes run different versions silently. Always use immutable tags: Git SHA or semantic version (<code>myapp:abc1234</code>).
</div>

<div class="callout-red callout">
  <strong>Running as root:</strong> A path traversal or deserialization exploit in your Spring Boot app gives the attacker root inside the container. Without <code>seccomp</code> profiles, root in container can mount host filesystems. K8s <code>securityContext.runAsNonRoot: true</code> enforces this at the platform level — but get it in the Dockerfile too.
</div>

<div class="callout-warn callout">
  <strong>No build cache layering:</strong> Copying source before dependencies means every source change re-downloads all Maven deps (~500 MB). At 10 commits/day on a team, that's 5 GB of redundant downloads per day per developer.
</div>

<hr>

<h2>AWS mapping</h2>

<table>
  <thead><tr><th>Local</th><th>AWS equivalent</th><th>Key difference</th></tr></thead>
  <tbody>
    <tr><td>Docker Hub / local registry</td><td>ECR (Elastic Container Registry)</td><td>Private, IAM-authenticated, image scanning built-in, no pull rate limits</td></tr>
    <tr><td><code>docker build</code> locally</td><td>CodeBuild or GitLab CI runner on EC2</td><td>Build runs in ephemeral environment; cache stored in S3</td></tr>
    <tr><td><code>docker run</code></td><td>ECS Fargate or EKS (K8s)</td><td>ECS = simpler, no K8s overhead; EKS = full K8s API, more control</td></tr>
    <tr><td>Local <code>.env</code> file</td><td>Secrets Manager + ECS task role / K8s ExternalSecrets</td><td>Never bake secrets into image; inject at runtime via IAM-authenticated secret fetch</td></tr>
  </tbody>
</table>

<hr>

<div class="quiz-section">
<h2>Quiz</h2>

<div class="question" id="q1">
  <p>Q1: Why copy <code>pom.xml</code> and run <code>mvn dependency:go-offline</code> before copying source?</p>
  <ul class="options" data-correct="2">
    <li>Maven requires dependencies before source files can be parsed</li>
    <li>It creates a separate Docker layer for dependencies that is cache-hit when only source changes — saving 2–3 minutes per build</li>
    <li>Docker cannot copy source and pom.xml in the same instruction</li>
    <li>It prevents Maven from downloading dependencies at runtime</li>
  </ul>
  <div class="explanation">Docker builds layers in order and caches each one. If <code>pom.xml</code> hasn't changed, the dependency layer is a cache hit — Maven download is skipped. Copying source first busts this cache on every commit because source always changes.</div>
</div>

<div class="question" id="q2">
  <p>Q2: A K8s pod receives SIGTERM from the kubelet but the Spring Boot app doesn't shut down gracefully — connections are dropped mid-request. What is the most likely Dockerfile cause?</p>
  <ul class="options" data-correct="3">
    <li>The JRE image is too large</li>
    <li>The pod is running as root</li>
    <li><code>CMD</code> uses shell form instead of exec form — SIGTERM goes to <code>sh</code>, not the JVM</li>
    <li>The <code>.dockerignore</code> is missing</li>
  </ul>
  <div class="explanation">Shell form (<code>CMD "java -jar app.jar"</code>) spawns <code>sh -c</code> as PID 1. SIGTERM hits <code>sh</code>, which doesn't forward it. The JVM never sees the signal → no graceful shutdown. Exec form (<code>CMD ["java", "-jar", "app.jar"]</code>) makes Java PID 1 directly — SIGTERM arrives at the JVM and Spring Boot's shutdown hooks fire.</div>
</div>

<div class="question" id="q3">
  <p>Q3: Your CI pipeline pushes <code>myapp:latest</code> to ECR. Three K8s nodes already have the image cached. A new deployment rolls out. What is the risk?</p>
  <ul class="options" data-correct="1">
    <li>Nodes with a cached <code>latest</code> image and <code>imagePullPolicy: IfNotPresent</code> will not pull the new image — pods on those nodes run the old version silently while new nodes run the new version</li>
    <li>ECR refuses to overwrite the <code>latest</code> tag</li>
    <li>K8s always pulls <code>latest</code> regardless of policy</li>
    <li>The deployment will fail with an image digest mismatch</li>
  </ul>
  <div class="explanation">With <code>imagePullPolicy: IfNotPresent</code> (the default for non-latest tags), nodes that already have the image skip the pull. But <code>latest</code> is a mutable tag — the cached image and the new image share the same tag with different digests. Result: mixed versions in prod with no visible indicator. Fix: always tag with an immutable identifier (Git SHA), and set <code>imagePullPolicy: Always</code> only as a last resort (it adds latency to every pod start).</div>
</div>

</div>

<hr>

<!-- DevOps Agentic AI Angle -->
<h2 style="border-color: #7c3aed">🤖 Agentic AI Angle</h2>

<h3>AI as workload — containerising an LLM inference service</h3>
<p>When the service inside the container IS an AI agent or LLM inference service, the Dockerfile changes significantly:</p>
<ul>
  <li><strong>Base image:</strong> <code>nvidia/cuda:12.x-runtime</code> instead of JRE — GPU drivers must be in the image</li>
  <li><strong>Model weights:</strong> never bake into the image (models are 4–70 GB). Mount from S3 at startup or use an init container to download. Image stays small and version-independent of model weights.</li>
  <li><strong>Startup time:</strong> LLM loading can take 30–120 seconds. K8s <code>startupProbe</code> with a long <code>failureThreshold</code> prevents premature SIGTERM during model load.</li>
  <li><strong>Memory:</strong> set <code>--memory</code> / K8s resource limit to prevent OOMKilled during inference spikes. Unlike a Java app, a single large prompt can spike memory 10×.</li>
</ul>

<pre>
<span class="c"># LLM inference service — key differences from standard Spring Boot Dockerfile</span>
<span class="kw">FROM</span> nvidia/cuda:<span class="nu">12.3.0-runtime-ubuntu22.04</span>

<span class="c"># Weights NOT in image — downloaded by init container from S3 at pod start.</span>
<span class="c"># Image tag = code version; model version pinned separately in ConfigMap.</span>
<span class="kw">ENV</span> MODEL_PATH=<span class="st">/models/llama-3-8b</span>

<span class="c"># Non-root still applies — LLM inference doesn't need root.</span>
<span class="kw">RUN</span> useradd -m mluser
<span class="kw">USER</span> mluser

<span class="kw">COPY</span> --from=build /app/inference_server app/
<span class="kw">CMD</span> [<span class="st">"./app/server"</span>, <span class="st">"--model-path"</span>, <span class="st">"$(MODEL_PATH)"</span>]
</pre>

<h3>AI as operator — Docker in an AI-powered CI pipeline</h3>
<ul>
  <li><strong>AI code reviewer in CI:</strong> GitLab CI stage calls Claude API to review the diff before the Docker build stage. If the review flags a critical issue, the pipeline fails before wasting time on a build.</li>
  <li><strong>AI-generated release notes:</strong> after <code>docker push</code>, a pipeline step calls an LLM to summarise the diff into human-readable release notes, posted to Slack.</li>
  <li><strong>Dockerfile linting agent:</strong> an agent analyses the Dockerfile for prod pitfalls (root user, latest tag, no .dockerignore) and comments on the MR before merge.</li>
</ul>

<h3>AWS managed services</h3>
<ul>
  <li><strong>ECR</strong> — private registry with built-in vulnerability scanning (ECR Enhanced Scanning via Inspector)</li>
  <li><strong>Bedrock</strong> — managed LLM inference; no Dockerfile needed for the model itself, but your agent container still follows all the above rules</li>
  <li><strong>SageMaker Inference</strong> — if self-hosting a model, SageMaker manages the GPU fleet; your container image is pushed to ECR and referenced in the endpoint config</li>
</ul>

<script>
document.querySelectorAll('.question').forEach(q => {
  const opts = q.querySelectorAll('.options li');
  const correct = parseInt(q.querySelector('.options').dataset.correct, 10) - 1;
  const explanation = q.querySelector('.explanation');
  opts.forEach((li, i) => {
    li.addEventListener('click', () => {
      opts.forEach(o => o.classList.remove('correct', 'wrong'));
      li.classList.add(i === correct ? 'correct' : 'wrong');
      opts[correct].classList.add('correct');
      explanation.style.display = 'block';
    });
  });
});
</script>

</body>
</html>
```

---

## Task 7: Create Seed Question q039

**Files:**
- Create: `devops/q039_dockerfile_spring_boot/question.md`
- Create: `devops/q039_dockerfile_spring_boot/MyAnswer.md`

- [ ] **Step 7.1: Create question.md**

```markdown
**Lessons:** [0001 Multi-Stage Dockerfile](../../../topics/devops/containers/0001-dockerfile-multistage.html)

# q039 — Containers: Production Dockerfile for Spring Boot

**Category:** DevOps · **Cluster:** containers · **Difficulty:** Easy · **Ideal time:** 15 min

---

## Task

Explain how you would write a production-ready Dockerfile for a Spring Boot microservice.

Cover:
1. Why multi-stage and what each stage does
2. Build cache layering strategy (why pom.xml before source)
3. Base image choice for the run stage
4. Non-root user — why and how
5. CMD exec form vs shell form — what breaks with shell form in K8s
6. .dockerignore — what to exclude and why
7. Image tagging strategy — why `latest` is dangerous in prod

For each decision, explain the **prod failure mode** that decision prevents.

---

## Constraints

- Answer is conceptual — no need to write syntactically perfect Dockerfile
- Prod pitfalls must be named, not just the happy path
- AWS equivalent for the registry should be mentioned

---

*Hint → `hint` | Submit → `done`*
```

- [ ] **Step 7.2: Create MyAnswer.md**

```markdown
# My Answer — q039 Dockerfile Spring Boot

## 1. Multi-stage build
[your explanation here]

## 2. Build cache layering
[your explanation here]

## 3. Base image for run stage
[your explanation here]

## 4. Non-root user
[your explanation here]

## 5. CMD exec form
[your explanation here]

## 6. .dockerignore
[your explanation here]

## 7. Image tagging
[your explanation here]
```

---

## Task 8: Commit

- [ ] **Step 8.1: Stage and commit all changes**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git add CLAUDE.md \
        devops/ \
        topics/devops/
git commit -m "feat: add devops section — 9 clusters, containers lesson, q039 seed question"
```

---

## Self-Review Against Spec

| Spec requirement | Task |
|---|---|
| One unified devops section | Task 1 (CLAUDE.md command legend) |
| 9 clusters defined | Task 1 + Task 2 |
| Anchor scenario documented | Task 6 (pipeline diagram in lesson HTML) |
| Concept-only MyAnswer.md | Task 7 |
| Lesson HTML: existing structure + AI angle at bottom | Task 6 |
| Review rubric: prod awareness = hard gate | Task 3 |
| Syntax = optional/bonus | Task 3 |
| Difficulty: easy/medium/hard/architecture | Task 3 |
| `next devops` rotation | Task 1 (routing description) |
| Slug override commands | Task 1 |
| Architecture = always hard | Task 1 |
| agent-infra = AI as workload | Task 6 AI section |
| ci-cd + observability = AI as operator | Task 6 AI section |
| Terraform cluster included | Task 1 + Task 2 |
| Folder scaffold for all 9 clusters | Task 5 |
| First working question end-to-end | Tasks 6 + 7 |
