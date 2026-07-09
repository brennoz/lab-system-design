# q039 — Dockerfile for Spring Boot

**Lessons:** [0001 Multi-Stage Dockerfile](../../../topics/devops/containers/0001-dockerfile-multistage.html)

**Category:** devops · **Cluster:** containers · **Difficulty:** easy  
**Ideal time:** 12 minutes

---

## Question

Write and explain a production-ready multi-stage Dockerfile for a Spring Boot 3 application.

Your answer must cover all of the following:

1. **Two FROM stages** — why do we use two stages? What goes in each?
2. **Layer caching trick** — how does the order of `COPY` commands affect rebuild speed when you change source code but not dependencies?
3. **Why `COPY pom.xml` before `COPY src`?** — explain the caching implication.
4. **Non-root user** — why does `USER appuser` matter in production? What's the security risk if you omit it?
5. **CMD form** — what's the difference between exec form (`CMD ["java", ...]`) and shell form (`CMD java ...`)? Which one works correctly in Kubernetes and why?
6. **One prod pitfall** — name a single container-specific pitfall that this Dockerfile should guard against. It must relate to containers (not Kubernetes HPA, not Terraform). Examples: secrets in image layers, running as root, missing .dockerignore, hardcoded configuration, `latest` tag, large image size.

## Constraints

- **No running code required** — this is a concept + annotated Dockerfile question
- Spring Boot 3, Maven build (pom.xml)
- Target Java 21
- Answer in `MyAnswer.md`: paste your Dockerfile with inline comments explaining each decision, then write prose sections for each of the 6 points above
- Your Dockerfile should be realistic and deployable (not pseudo-code)

## Rubric — Pass requires

| Item | Must cover |
|---|---|
| Two stages | Builder and runtime stages identified; purpose of each stated |
| Layer cache | Explains why `pom.xml` copy + install is separate from source code copy |
| pom.xml first | Correctly states that Maven deps layer caches independently if src changes |
| Non-root user | Explains privilege escalation risk and why `USER appuser` is required for prod |
| CMD form | Names both forms, explains `exec` form is PID 1 signal handling, shell form doesn't receive SIGTERM correctly, Kubernetes needs exec form |
| Prod pitfall | Names a specific container pitfall and consequence (not K8s-level, not infra-level) |

## Tips

- Multi-stage `FROM ... AS builder` then `FROM ... AS final` is standard.
- Layer caching: every `RUN`, `COPY`, `ADD` is a layer. If layer N hasn't changed, Docker reuses it.
- Exec form vs shell form: `exec` replaces the shell and becomes PID 1 directly; shell form wraps the command, missing signals.
- Common pitfall: hardcoded secrets, no `.dockerignore`, `latest` tag, large image from bloat, or running as root.

---

## Answer format

Paste your complete Dockerfile in a code block with inline comments. Then answer each of the 6 points in short prose paragraphs (2–3 sentences each is enough). Keep it concise.
