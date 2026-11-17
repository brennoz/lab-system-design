# InterviewDoctor — Design Spec
**Date:** 2026-06-12
**Status:** Approved

---

## 1. What We're Building

InterviewDoctor is a CV + Job Description analyzer for software engineers.

User pastes their CV and a job description → AI returns a readiness score, a list of skill gaps, and a list of confirmed strengths. No company name references. No login required for v0. Completely free to run.

**One-line value prop:** "Find out exactly what's missing before you apply."

---

## 2. Scope

### V0 — Stateless Analyzer (ship first)
- CV + JD text form
- Groq llama-3.3-70b analysis (free tier)
- Readiness score (0–100%)
- Skill gaps list (with severity: critical / minor)
- Strengths list
- One-line AI summary
- Rate limiting (per-IP, in-memory)
- Security headers
- Vercel Analytics (page views, unique visitors)
- Medical diagnostic terminal UI

### V1 — Community Layer (next milestone)
- User accounts (Supabase auth — email/password)
- Community resource submissions (study plans, links, courses)
- "How many weeks?" input → week-by-week study plan
- Upstash Redis rate limiting (replaces in-memory)
- Supabase PostgreSQL (resources table + users)
- Row Level Security on all tables

---

## 3. Architecture

### V0 Data Flow
```
Browser
  │  { cv, jobDescription }
  ▼
middleware.ts  — rate limit check (per IP)
  │
  ▼
app/actions/analyze.ts  (Next.js Server Action)
  │  1. sanitize inputs
  │  2. build structured prompt
  │  3. call Groq llama-3.3-70b
  │  4. parse JSON response
  ▼
results/page.tsx  — renders ScoreCard + GapList
```

### V1 Additional Flow
```
app/actions/resources.ts  (Server Action)
  │  createResource / listResources
  ▼
Supabase PostgreSQL (free tier, 500MB)
  └── Row Level Security enforced at DB level
```

### What does NOT exist
- No Spring Boot backend
- No microservices
- No Docker
- No message queue
- No Redis in v0
- No separate deployment for the backend

---

## 4. Tech Stack

| Concern | Choice | Why |
|---|---|---|
| Framework | Next.js 14+ App Router | Frontend + API in one Vercel project |
| Language | TypeScript | Type safety throughout |
| AI | Groq llama-3.3-70b | 14,400 req/day free, no credit card |
| Styling | Tailwind CSS | Utility-first, works with App Router |
| Hosting | Vercel (free hobby tier) | Native Next.js, auto-deploys on push |
| Analytics | Vercel Analytics | Free, zero config, page views + visitors |
| DB (v1) | Supabase free tier | PostgreSQL + auth + RLS in one service |
| Redis (v1) | Upstash free tier | 10k commands/day, serverless-native |
| CI | GitHub Actions | Type check + lint on every push |

---

## 5. Folder Structure

```
interview-doctor/
├── .github/
│   └── workflows/
│       └── ci.yml              — npm ci, tsc, eslint
├── app/
│   ├── layout.tsx              — root layout, fonts, CSP headers
│   ├── page.tsx                — home: analyzer form
│   ├── results/
│   │   └── page.tsx            — score + gap list
│   └── actions/
│       └── analyze.ts          — Server Action: Groq call
├── components/
│   ├── AnalyzerForm.tsx        — CV + JD textareas + submit
│   ├── ScoreCard.tsx           — score % with progress bar
│   └── GapList.tsx             — gaps + strengths columns
├── lib/
│   ├── gemini.ts               — Groq SDK wrapper
│   ├── sanitize.ts             — strip HTML, truncate to 8k chars
│   └── rateLimit.ts            — per-IP in-memory store
├── middleware.ts               — rate limit + CORS enforcement
├── .env.local                  — gitignored, local dev only
├── .env.example                — committed, shows required vars
├── next.config.ts              — CSP, X-Frame-Options, CORS headers
├── package.json
└── tsconfig.json
```

---

## 6. AI Prompt Design

```
You are a technical hiring expert. Analyze this candidate's CV
against the job description.

Return ONLY valid JSON in this exact schema — no markdown, no explanation:
{
  "score": <integer 0-100>,
  "gaps": [{ "skill": string, "severity": "critical" | "minor" }],
  "strengths": [{ "skill": string }],
  "summary": <one sentence, max 20 words>
}

CV:
---
{sanitized cv text, max 8000 chars}

JOB DESCRIPTION:
---
{sanitized jd text, max 8000 chars}
```

**Why strict JSON schema:** LLMs return free-form text by default. Explicit schema makes the response deterministically parseable. If parsing fails → retry once → then return user-facing error. Never render raw LLM output (XSS risk).

---

## 7. Security

| Layer | Mechanism | Reason |
|---|---|---|
| API key | Server-side env var only | Never reaches browser |
| Input | Strip HTML + truncate before prompt | Prompt injection + context overflow prevention |
| Rate limiting | Per-IP in middleware (v0: in-memory, v1: Upstash) | Protect Groq free quota from abuse |
| CORS | Locked to own domain in `next.config.ts` | Block external API callers |
| HTTPS | Vercel free TLS | In-transit encryption |
| Headers | CSP, X-Frame-Options, X-Content-Type-Options | XSS + clickjacking prevention |
| DB (v1) | Supabase Row Level Security | Users can only modify their own rows — enforced at DB level, not app level |
| Auth (v1) | Supabase JWT — never custom | Avoid auth implementation vulnerabilities |

---

## 8. UI Design

**Aesthetic:** Medical Diagnostic Terminal. Dark background, medical monitor green, full monospace typography. Results displayed as a diagnostic report. Memorable, on-brand with the "Doctor" metaphor.

**Palette:**
```
Background:    #030d07
Card surface:  #0a1a0f
Primary green: #00e87a
Cold blue:     #00b8d9  (strengths)
Danger red:    #ff4d4d  (gaps)
Text:          #d4f0e0
Muted text:    #7aad8e
Border:        #1a3d26
```

**Typography:** JetBrains Mono (Google Fonts, free) — monospace throughout.

**Motion:**
- ECG scanning line while Groq processes
- Score counts up 0 → final value on reveal
- Gap/strength items stagger in with delay
- Button pulse on hover

**Landing page:** Two side-by-side textareas (CV left, JD right) + RUN DIAGNOSTIC button below.

**Results page:** Score gauge (center) + two columns (GAPS DETECTED left, STRENGTHS CONFIRMED right) + RUN ANOTHER SCAN button.

---

## 9. CI/CD Pipeline

```
git push origin main
        │
        ├──► GitHub Actions (parallel)
        │         tsc --noEmit + eslint
        │         Reports ✓/✗ on commit (informational in v0)
        │
        └──► Vercel webhook (parallel)
                  npm run build → deploy to CDN
                  Live in ~30 seconds
```

Preview deployments: every PR gets a unique `*.vercel.app` URL automatically.

**Environment variables:**
- Local dev: `.env.local` (gitignored)
- Production: Vercel dashboard → Environment Variables
- Documented: `.env.example` (committed, values empty)

---

## 10. GitHub Issues — V0 Milestone

| # | Title |
|---|---|
| 1 | Project scaffold: Next.js 14 + Tailwind + TypeScript |
| 2 | Security headers + CORS in next.config.ts |
| 3 | Input sanitization (lib/sanitize.ts) |
| 4 | Groq SDK wrapper (lib/gemini.ts) |
| 5 | Rate limiting middleware (per-IP in-memory) |
| 6 | Server Action: analyze CV+JD (app/actions/analyze.ts) |
| 7 | AnalyzerForm component (landing page UI) |
| 8 | ScoreCard + GapList components (results page UI) |
| 9 | Vercel Analytics integration |
| 10 | GitHub Actions CI (tsc + eslint) |
| 11 | Deploy to Vercel + smoke test |

---

## 11. V1 GitHub Issues (backlog)

| # | Title |
|---|---|
| 12 | Supabase project setup + schema migration |
| 13 | Supabase auth (email/password sign up + login) |
| 14 | Community resources: submit + list (Server Actions) |
| 15 | Resources page UI |
| 16 | Upstash Redis rate limiting (replace in-memory) |
| 17 | "Weeks available" input + study plan generation |
| 18 | Study plan results section UI |

---

## 12. Free Tier Cost Breakdown

| Service | Free Limit | Usage estimate |
|---|---|---|
| Vercel | Unlimited hobby deploys | N/A |
| Groq llama-3.3-70b | 1,500 req/day | ~50 users/day comfortable |
| Vercel Analytics | 2,500 events/month | Fine for early traffic |
| Supabase (v1) | 500MB PostgreSQL | Years of community resources |
| Upstash Redis (v1) | 10k commands/day | Fine for rate limiting |
| GitHub | Unlimited | N/A |

**Total monthly cost: £0**
