# InterviewDoctor V0 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a stateless CV + Job Description analyzer that calls Gemini 1.5 Flash and returns a readiness score + skill gap list, deployed free on Vercel.

**Architecture:** Next.js 14 App Router monolith. User submits CV + JD via a form backed by a Server Action. The Server Action sanitizes inputs, calls Gemini 1.5 Flash, parses the JSON response, and returns the result to the client via `useActionState`. No database, no auth, no separate backend.

**Tech Stack:** Next.js 14, TypeScript, Tailwind CSS, `@google/generative-ai` SDK, `@vercel/analytics`, Vitest for unit tests, GitHub Actions for CI, Vercel for hosting.

---

## File Map

| File | Responsibility |
|---|---|
| `app/layout.tsx` | Root layout: JetBrains Mono font, Vercel Analytics, background color |
| `app/page.tsx` | Home page: renders `AnalyzerForm` |
| `app/globals.css` | CSS variables for medical terminal palette |
| `app/actions/analyze.ts` | Server Action: sanitize → Gemini → parse JSON → return result |
| `components/AnalyzerForm.tsx` | Client Component: form + `useActionState` + renders results |
| `components/ScoreCard.tsx` | Score display: animated count-up + progress bar |
| `components/GapList.tsx` | Two-column: gaps (red) + strengths (blue) |
| `lib/gemini.ts` | Gemini SDK wrapper: single `analyzeWithGemini(prompt)` function |
| `lib/sanitize.ts` | Strip HTML tags, truncate to 8000 chars, trim whitespace |
| `lib/rateLimit.ts` | Per-IP in-memory rate limit: 5 req/min, `resetAll()` for tests |
| `next.config.ts` | Security headers: CSP, X-Frame-Options, X-Content-Type-Options |
| `vitest.config.ts` | Vitest with jsdom + `@/` path alias |
| `.env.example` | Documents `GEMINI_API_KEY=` (value empty) |
| `.github/workflows/ci.yml` | tsc + eslint + vitest on every push/PR |

---

## Task 1: Scaffold the project

**Files:**
- Create: `/home/brennoz77/LABS/InterviewDoctor/` (new project root, outside LAB_CLAUDE)

- [ ] **Step 1: Create Next.js app**

```bash
cd /home/brennoz77/LABS
npx create-next-app@latest InterviewDoctor \
  --typescript \
  --tailwind \
  --app \
  --no-src-dir \
  --import-alias "@/*" \
  --no-eslint
```

When prompted: select defaults. This creates the project with App Router, TypeScript, Tailwind.

- [ ] **Step 2: Install dependencies**

```bash
cd /home/brennoz77/LABS/InterviewDoctor
npm install @google/generative-ai @vercel/analytics
npm install -D vitest @vitejs/plugin-react jsdom
```

- [ ] **Step 3: Add test script to package.json**

Open `package.json`. In `"scripts"`, add:
```json
"test": "vitest run",
"test:watch": "vitest"
```

- [ ] **Step 4: Remove boilerplate**

Delete `app/page.tsx` contents (will replace in Task 12).
Delete everything inside `app/globals.css` (will replace in Task 8).

- [ ] **Step 5: Verify the scaffold builds**

```bash
npm run build
```

Expected: `✓ Compiled successfully`

- [ ] **Step 6: Initialise git**

```bash
git init
git add .
git commit -m "scaffold Next.js 14 with TypeScript, Tailwind, App Router"
```

---

## Task 2: Vitest configuration

**Files:**
- Create: `vitest.config.ts`

- [ ] **Step 1: Create vitest.config.ts**

```typescript
// vitest.config.ts
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  test: {
    environment: 'jsdom',
    globals: true,
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, '.'),
    },
  },
})
```

- [ ] **Step 2: Verify Vitest runs**

```bash
npx vitest run
```

Expected: `No test files found` (no tests yet — this is fine)

- [ ] **Step 3: Commit**

```bash
git add vitest.config.ts package.json
git commit -m "add Vitest with jsdom and @/ alias"
```

---

## Task 3: Security headers

**Files:**
- Modify: `next.config.ts`
- Create: `.env.example`

- [ ] **Step 1: Replace next.config.ts**

```typescript
// next.config.ts
import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          { key: 'X-Frame-Options', value: 'DENY' },
          { key: 'X-Content-Type-Options', value: 'nosniff' },
          { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
          {
            key: 'Content-Security-Policy',
            value: [
              "default-src 'self'",
              "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
              "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
              "font-src 'self' https://fonts.gstatic.com",
              "connect-src 'self' https://vitals.vercel-insights.com",
            ].join('; '),
          },
        ],
      },
    ]
  },
}

export default nextConfig
```

- [ ] **Step 2: Create .env.example**

```
# Copy this file to .env.local and fill in the values
GEMINI_API_KEY=
```

- [ ] **Step 3: Verify .env.local is gitignored**

```bash
cat .gitignore | grep env
```

Expected output includes `.env*.local`

- [ ] **Step 4: Commit**

```bash
git add next.config.ts .env.example
git commit -m "add security headers and .env.example"
```

---

## Task 4: Sanitize library (TDD)

**Files:**
- Create: `lib/sanitize.ts`
- Create: `lib/__tests__/sanitize.test.ts`

- [ ] **Step 1: Write the failing test**

```typescript
// lib/__tests__/sanitize.test.ts
import { describe, it, expect } from 'vitest'
import { sanitize } from '@/lib/sanitize'

describe('sanitize', () => {
  it('strips HTML tags', () => {
    expect(sanitize('<script>alert(1)</script>Hello')).toBe('Hello')
  })

  it('truncates to 8000 chars', () => {
    const long = 'a'.repeat(9000)
    expect(sanitize(long)).toHaveLength(8000)
  })

  it('trims whitespace', () => {
    expect(sanitize('  hello  ')).toBe('hello')
  })

  it('returns empty string for empty input', () => {
    expect(sanitize('')).toBe('')
  })
})
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
npx vitest run lib/__tests__/sanitize.test.ts
```

Expected: `FAIL — Cannot find module '@/lib/sanitize'`

- [ ] **Step 3: Implement sanitize.ts**

```typescript
// lib/sanitize.ts
const MAX_LENGTH = 8000

export function sanitize(input: string): string {
  return input
    .replace(/<[^>]*>/g, '')
    .trim()
    .slice(0, MAX_LENGTH)
}
```

- [ ] **Step 4: Run test to confirm it passes**

```bash
npx vitest run lib/__tests__/sanitize.test.ts
```

Expected: `✓ 4 tests passed`

- [ ] **Step 5: Commit**

```bash
git add lib/sanitize.ts lib/__tests__/sanitize.test.ts
git commit -m "add sanitize: strip HTML, truncate, trim (TDD)"
```

---

## Task 5: Rate limit library (TDD)

**Files:**
- Create: `lib/rateLimit.ts`
- Create: `lib/__tests__/rateLimit.test.ts`

- [ ] **Step 1: Write the failing test**

```typescript
// lib/__tests__/rateLimit.test.ts
import { describe, it, expect, beforeEach } from 'vitest'
import { checkRateLimit, resetAll } from '@/lib/rateLimit'

describe('checkRateLimit', () => {
  beforeEach(() => resetAll())

  it('allows the first request from an IP', () => {
    expect(checkRateLimit('1.2.3.4').allowed).toBe(true)
  })

  it('blocks after MAX_REQUESTS in the same window', () => {
    for (let i = 0; i < 5; i++) checkRateLimit('1.2.3.4')
    const result = checkRateLimit('1.2.3.4')
    expect(result.allowed).toBe(false)
    expect(result.retryAfter).toBeGreaterThan(0)
  })

  it('tracks different IPs independently', () => {
    for (let i = 0; i < 5; i++) checkRateLimit('1.1.1.1')
    expect(checkRateLimit('2.2.2.2').allowed).toBe(true)
  })
})
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
npx vitest run lib/__tests__/rateLimit.test.ts
```

Expected: `FAIL — Cannot find module '@/lib/rateLimit'`

- [ ] **Step 3: Implement rateLimit.ts**

```typescript
// lib/rateLimit.ts
const WINDOW_MS = 60_000
const MAX_REQUESTS = 5

type Entry = { count: number; resetAt: number }
const store = new Map<string, Entry>()

export function checkRateLimit(ip: string): { allowed: boolean; retryAfter?: number } {
  const now = Date.now()
  const entry = store.get(ip)

  if (!entry || now > entry.resetAt) {
    store.set(ip, { count: 1, resetAt: now + WINDOW_MS })
    return { allowed: true }
  }

  if (entry.count >= MAX_REQUESTS) {
    return { allowed: false, retryAfter: Math.ceil((entry.resetAt - now) / 1000) }
  }

  entry.count++
  return { allowed: true }
}

// exported for test cleanup only
export function resetAll(): void {
  store.clear()
}
```

- [ ] **Step 4: Run test to confirm it passes**

```bash
npx vitest run lib/__tests__/rateLimit.test.ts
```

Expected: `✓ 3 tests passed`

- [ ] **Step 5: Commit**

```bash
git add lib/rateLimit.ts lib/__tests__/rateLimit.test.ts
git commit -m "add rate limiter: 5 req/min per IP, in-memory (TDD)"
```

---

## Task 6: Gemini SDK wrapper (TDD)

**Files:**
- Create: `lib/gemini.ts`
- Create: `lib/__tests__/gemini.test.ts`

- [ ] **Step 1: Create .env.local with your Gemini key**

Get a free key at https://aistudio.google.com/app/apikey (sign in with Google, click "Create API key").

```bash
echo "GEMINI_API_KEY=your_key_here" > .env.local
```

Replace `your_key_here` with the actual key.

- [ ] **Step 2: Write the failing test**

```typescript
// lib/__tests__/gemini.test.ts
import { describe, it, expect, vi } from 'vitest'

vi.mock('@google/generative-ai', () => ({
  GoogleGenerativeAI: vi.fn().mockImplementation(() => ({
    getGenerativeModel: vi.fn().mockReturnValue({
      generateContent: vi.fn().mockResolvedValue({
        response: {
          text: () => '{"score":80,"gaps":[],"strengths":[],"summary":"Strong match."}',
        },
      }),
    }),
  })),
}))

import { analyzeWithGemini } from '@/lib/gemini'

describe('analyzeWithGemini', () => {
  it('returns text from Gemini response', async () => {
    const result = await analyzeWithGemini('test prompt')
    expect(result).toContain('"score"')
  })
})
```

- [ ] **Step 3: Run test to confirm it fails**

```bash
npx vitest run lib/__tests__/gemini.test.ts
```

Expected: `FAIL — Cannot find module '@/lib/gemini'`

- [ ] **Step 4: Implement gemini.ts**

```typescript
// lib/gemini.ts
import { GoogleGenerativeAI } from '@google/generative-ai'

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY!)

const model = genAI.getGenerativeModel({ model: 'gemini-1.5-flash' })

export async function analyzeWithGemini(prompt: string): Promise<string> {
  const result = await model.generateContent(prompt)
  return result.response.text()
}
```

- [ ] **Step 5: Run test to confirm it passes**

```bash
npx vitest run lib/__tests__/gemini.test.ts
```

Expected: `✓ 1 test passed`

- [ ] **Step 6: Commit**

```bash
git add lib/gemini.ts lib/__tests__/gemini.test.ts
git commit -m "add Gemini 1.5 Flash wrapper (TDD)"
```

---

## Task 7: Server Action (TDD)

**Files:**
- Create: `app/actions/analyze.ts`
- Create: `app/actions/__tests__/analyze.test.ts`

- [ ] **Step 1: Write the failing test**

```typescript
// app/actions/__tests__/analyze.test.ts
import { describe, it, expect, vi } from 'vitest'

vi.mock('@/lib/gemini', () => ({
  analyzeWithGemini: vi.fn().mockResolvedValue(
    '{"score":75,"gaps":[{"skill":"RAG","severity":"critical"}],"strengths":[{"skill":"Kafka"}],"summary":"Good backend engineer."}'
  ),
}))

import { analyzeAction } from '@/app/actions/analyze'

describe('analyzeAction', () => {
  it('returns success with parsed result when inputs are valid', async () => {
    const formData = new FormData()
    formData.set('cv', 'Senior Java developer with 5 years Kafka experience')
    formData.set('jobDescription', 'AI engineer role requiring RAG and vector databases')

    const state = await analyzeAction({ status: 'idle' }, formData)

    expect(state.status).toBe('success')
    if (state.status === 'success') {
      expect(state.result.score).toBe(75)
      expect(state.result.gaps[0].skill).toBe('RAG')
      expect(state.result.strengths[0].skill).toBe('Kafka')
    }
  })

  it('returns error when CV is empty', async () => {
    const formData = new FormData()
    formData.set('cv', '')
    formData.set('jobDescription', 'Some job description')

    const state = await analyzeAction({ status: 'idle' }, formData)

    expect(state.status).toBe('error')
  })
})
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
npx vitest run app/actions/__tests__/analyze.test.ts
```

Expected: `FAIL — Cannot find module '@/app/actions/analyze'`

- [ ] **Step 3: Create the actions directory and implement analyze.ts**

```typescript
// app/actions/analyze.ts
'use server'

import { sanitize } from '@/lib/sanitize'
import { analyzeWithGemini } from '@/lib/gemini'
import { checkRateLimit } from '@/lib/rateLimit'
import { headers } from 'next/headers'

export type AnalysisResult = {
  score: number
  gaps: { skill: string; severity: 'critical' | 'minor' }[]
  strengths: { skill: string }[]
  summary: string
}

export type AnalysisState =
  | { status: 'idle' }
  | { status: 'success'; result: AnalysisResult }
  | { status: 'error'; message: string }

const PROMPT_TEMPLATE = (cv: string, jd: string) => `
You are a technical hiring expert. Analyze this candidate's CV against the job description.

Return ONLY valid JSON in this exact schema — no markdown, no explanation:
{
  "score": <integer 0-100>,
  "gaps": [{ "skill": string, "severity": "critical" | "minor" }],
  "strengths": [{ "skill": string }],
  "summary": "<one sentence, max 20 words>"
}

CV:
---
${cv}

JOB DESCRIPTION:
---
${jd}
`.trim()

export async function analyzeAction(
  _prevState: AnalysisState,
  formData: FormData
): Promise<AnalysisState> {
  const cv = sanitize(formData.get('cv') as string ?? '')
  const jd = sanitize(formData.get('jobDescription') as string ?? '')

  if (!cv || !jd) {
    return { status: 'error', message: 'Both CV and job description are required.' }
  }

  const headersList = await headers()
  const ip = headersList.get('x-forwarded-for')?.split(',')[0] ?? 'unknown'
  const rateCheck = checkRateLimit(ip)
  if (!rateCheck.allowed) {
    return {
      status: 'error',
      message: `Too many requests. Please wait ${rateCheck.retryAfter} seconds.`,
    }
  }

  try {
    const text = await analyzeWithGemini(PROMPT_TEMPLATE(cv, jd))
    const clean = text.replace(/```json\n?/g, '').replace(/```\n?/g, '').trim()
    const result = JSON.parse(clean) as AnalysisResult
    return { status: 'success', result }
  } catch {
    return { status: 'error', message: 'Diagnosis failed. Please try again.' }
  }
}
```

- [ ] **Step 4: Run test to confirm it passes**

```bash
npx vitest run app/actions/__tests__/analyze.test.ts
```

Expected: `✓ 2 tests passed`

- [ ] **Step 5: Run all tests**

```bash
npx vitest run
```

Expected: `✓ 9 tests passed`

- [ ] **Step 6: Commit**

```bash
git add app/actions/
git commit -m "add analyzeAction Server Action: sanitize, rate limit, Gemini (TDD)"
```

---

## Task 8: Global styles (medical terminal theme)

**Files:**
- Modify: `app/globals.css`
- Modify: `app/layout.tsx`

- [ ] **Step 1: Replace app/globals.css**

```css
/* app/globals.css */
@import "tailwindcss";

:root {
  --bg: #030d07;
  --surface: #0a1a0f;
  --green: #00e87a;
  --blue: #00b8d9;
  --red: #ff4d4d;
  --text: #d4f0e0;
  --muted: #7aad8e;
  --border: #1a3d26;
}

* {
  box-sizing: border-box;
}

body {
  background-color: var(--bg);
  color: var(--text);
}

::selection {
  background: var(--green);
  color: var(--bg);
}

::-webkit-scrollbar {
  width: 4px;
}

::-webkit-scrollbar-track {
  background: var(--bg);
}

::-webkit-scrollbar-thumb {
  background: var(--border);
}

textarea::placeholder {
  color: var(--muted);
  opacity: 0.5;
}
```

- [ ] **Step 2: Replace app/layout.tsx**

```typescript
// app/layout.tsx
import type { Metadata } from 'next'
import { JetBrains_Mono } from 'next/font/google'
import { Analytics } from '@vercel/analytics/react'
import './globals.css'

const jetbrainsMono = JetBrains_Mono({
  subsets: ['latin'],
  variable: '--font-mono',
  display: 'swap',
})

export const metadata: Metadata = {
  title: 'InterviewDoctor — Diagnose your readiness',
  description: 'Paste your CV and job description. Find out exactly what skills you are missing.',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={jetbrainsMono.variable}>
      <body className="font-mono min-h-screen bg-[#030d07] text-[#d4f0e0] antialiased">
        {children}
        <Analytics />
      </body>
    </html>
  )
}
```

- [ ] **Step 3: Verify build still passes**

```bash
npm run build
```

Expected: `✓ Compiled successfully`

- [ ] **Step 4: Commit**

```bash
git add app/globals.css app/layout.tsx
git commit -m "add medical terminal theme: JetBrains Mono, dark palette, CSS vars"
```

---

## Task 9: ScoreCard component

**Files:**
- Create: `components/ScoreCard.tsx`

- [ ] **Step 1: Create ScoreCard.tsx**

```typescript
// components/ScoreCard.tsx
'use client'

import { useEffect, useState } from 'react'

type Props = {
  score: number
  summary: string
}

export default function ScoreCard({ score, summary }: Props) {
  const [displayed, setDisplayed] = useState(0)

  useEffect(() => {
    setDisplayed(0)
    let frame = 0
    const totalFrames = 40
    const timer = setInterval(() => {
      frame++
      setDisplayed(Math.round((score * frame) / totalFrames))
      if (frame >= totalFrames) clearInterval(timer)
    }, 20)
    return () => clearInterval(timer)
  }, [score])

  const color =
    score >= 70 ? '#00e87a' : score >= 40 ? '#f59e0b' : '#ff4d4d'
  const filledBars = Math.round(displayed / 5)

  return (
    <div className="mb-12 text-center">
      <div className="text-xs tracking-widest mb-6" style={{ color: '#7aad8e' }}>
        READINESS SCORE
      </div>

      <div
        className="text-8xl font-bold mb-6 tabular-nums"
        style={{ color }}
      >
        {displayed}%
      </div>

      <div className="flex justify-center gap-1 mb-6">
        {Array.from({ length: 20 }).map((_, i) => (
          <div
            key={i}
            className="h-2 w-8 rounded-sm transition-colors duration-100"
            style={{ backgroundColor: i < filledBars ? color : '#1a3d26' }}
          />
        ))}
      </div>

      <p className="text-sm italic" style={{ color: '#7aad8e' }}>
        {summary}
      </p>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add components/ScoreCard.tsx
git commit -m "add ScoreCard: animated count-up, colour-coded progress bar"
```

---

## Task 10: GapList component

**Files:**
- Create: `components/GapList.tsx`

- [ ] **Step 1: Create GapList.tsx**

```typescript
// components/GapList.tsx
type Gap = { skill: string; severity: 'critical' | 'minor' }
type Strength = { skill: string }

type Props = {
  gaps: Gap[]
  strengths: Strength[]
}

export default function GapList({ gaps, strengths }: Props) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-12 mt-4">
      <div>
        <div
          className="text-xs tracking-widest mb-4 pb-2 border-b"
          style={{ color: '#ff4d4d', borderColor: '#1a3d26' }}
        >
          GAPS DETECTED
        </div>
        {gaps.length === 0 ? (
          <p className="text-sm" style={{ color: '#7aad8e' }}>
            No gaps detected.
          </p>
        ) : (
          <ul className="space-y-3">
            {gaps.map((gap, i) => (
              <li
                key={i}
                className="text-sm flex items-baseline gap-2"
                style={{
                  opacity: 0,
                  animation: `fadeIn 0.3s ease forwards`,
                  animationDelay: `${i * 80}ms`,
                }}
              >
                <span style={{ color: '#ff4d4d' }}>✗</span>
                <span style={{ color: '#d4f0e0' }}>{gap.skill}</span>
                {gap.severity === 'critical' && (
                  <span className="ml-auto text-xs" style={{ color: '#ff4d4d' }}>
                    [critical]
                  </span>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>

      <div>
        <div
          className="text-xs tracking-widest mb-4 pb-2 border-b"
          style={{ color: '#00b8d9', borderColor: '#1a3d26' }}
        >
          STRENGTHS CONFIRMED
        </div>
        {strengths.length === 0 ? (
          <p className="text-sm" style={{ color: '#7aad8e' }}>
            No strengths detected.
          </p>
        ) : (
          <ul className="space-y-3">
            {strengths.map((s, i) => (
              <li
                key={i}
                className="text-sm flex items-baseline gap-2"
                style={{
                  opacity: 0,
                  animation: `fadeIn 0.3s ease forwards`,
                  animationDelay: `${i * 80}ms`,
                }}
              >
                <span style={{ color: '#00b8d9' }}>✓</span>
                <span style={{ color: '#d4f0e0' }}>{s.skill}</span>
              </li>
            ))}
          </ul>
        )}
      </div>

      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(4px); }
          to   { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add components/GapList.tsx
git commit -m "add GapList: two-column gaps/strengths with stagger animation"
```

---

## Task 11: AnalyzerForm component

**Files:**
- Create: `components/AnalyzerForm.tsx`

- [ ] **Step 1: Create AnalyzerForm.tsx**

```typescript
// components/AnalyzerForm.tsx
// Note: uses useFormState + useFormStatus (React 18 / Next.js 14)
// useActionState is React 19+ only; useFormState is the React 18 equivalent
'use client'

import { useFormState, useFormStatus } from 'react-dom'
import { analyzeAction, AnalysisState } from '@/app/actions/analyze'
import ScoreCard from '@/components/ScoreCard'
import GapList from '@/components/GapList'

const initialState: AnalysisState = { status: 'idle' }

// Must be a child of <form> to access useFormStatus
function SubmitButton() {
  const { pending } = useFormStatus()
  return (
    <button
      type="submit"
      disabled={pending}
      className="border px-16 py-4 text-sm tracking-widest transition-all duration-300 disabled:opacity-40 disabled:cursor-not-allowed"
      style={{ borderColor: '#00e87a', color: '#00e87a' }}
      onMouseEnter={(e) => {
        if (!pending) {
          ;(e.target as HTMLButtonElement).style.backgroundColor = '#00e87a'
          ;(e.target as HTMLButtonElement).style.color = '#030d07'
        }
      }}
      onMouseLeave={(e) => {
        ;(e.target as HTMLButtonElement).style.backgroundColor = 'transparent'
        ;(e.target as HTMLButtonElement).style.color = '#00e87a'
      }}
    >
      {pending ? '◌  SCANNING...' : '⬡  RUN DIAGNOSTIC'}
    </button>
  )
}

export default function AnalyzerForm() {
  const [state, formAction] = useFormState(analyzeAction, initialState)

  return (
    <div className="max-w-5xl mx-auto px-6 py-12">
      {/* Header */}
      <header className="mb-12">
        <div
          className="text-xs tracking-widest mb-3 flex items-center gap-2"
          style={{ color: '#00e87a' }}
        >
          <span>INTERVIEW</span>
          <span style={{ color: '#1a3d26' }}>●</span>
          <span>DOCTOR</span>
        </div>
        <h1 className="text-5xl font-bold mb-4" style={{ color: '#d4f0e0' }}>
          DIAGNOSTIC SCAN_
        </h1>
        <p className="text-sm" style={{ color: '#7aad8e' }}>
          Paste your CV and job description.{' '}
          <span style={{ color: '#d4f0e0' }}>We tell you exactly how ready you are.</span>
        </p>
      </header>

      {/* Separator */}
      <div className="mb-10 h-px" style={{ backgroundColor: '#1a3d26' }} />

      {/* Form */}
      <form action={formAction}>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div>
            <label
              className="block text-xs tracking-widest mb-2"
              style={{ color: '#00e87a' }}
            >
              // YOUR CV
            </label>
            <textarea
              name="cv"
              rows={14}
              required
              placeholder="Paste your CV or resume here..."
              className="w-full bg-[#0a1a0f] border p-4 text-sm resize-none focus:outline-none transition-colors"
              style={{
                borderColor: '#1a3d26',
                color: '#d4f0e0',
                fontFamily: 'inherit',
              }}
              onFocus={(e) => (e.target.style.borderColor = '#00e87a')}
              onBlur={(e) => (e.target.style.borderColor = '#1a3d26')}
            />
          </div>
          <div>
            <label
              className="block text-xs tracking-widest mb-2"
              style={{ color: '#00e87a' }}
            >
              // JOB DESCRIPTION
            </label>
            <textarea
              name="jobDescription"
              rows={14}
              required
              placeholder="Paste the job description here..."
              className="w-full bg-[#0a1a0f] border p-4 text-sm resize-none focus:outline-none transition-colors"
              style={{
                borderColor: '#1a3d26',
                color: '#d4f0e0',
                fontFamily: 'inherit',
              }}
              onFocus={(e) => (e.target.style.borderColor = '#00e87a')}
              onBlur={(e) => (e.target.style.borderColor = '#1a3d26')}
            />
          </div>
        </div>

        <div className="flex justify-center">
          <SubmitButton />
        </div>
      </form>

      {/* Error */}
      {state.status === 'error' && (
        <div
          className="mt-8 border p-4 text-sm"
          style={{ borderColor: '#ff4d4d', color: '#ff4d4d' }}
        >
          ✗ {state.message}
        </div>
      )}

      {/* Results */}
      {state.status === 'success' && (
        <div className="mt-16">
          <div
            className="text-xs tracking-widest mb-10 pb-4 border-b"
            style={{ color: '#00e87a', borderColor: '#1a3d26' }}
          >
            DIAGNOSIS REPORT
          </div>
          <ScoreCard score={state.result.score} summary={state.result.summary} />
          <GapList gaps={state.result.gaps} strengths={state.result.strengths} />
          <div className="flex justify-center mt-12">
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="border px-12 py-3 text-sm tracking-widest transition-colors"
              style={{ borderColor: '#1a3d26', color: '#7aad8e' }}
              onMouseEnter={(e) => {
                ;(e.target as HTMLButtonElement).style.borderColor = '#7aad8e'
              }}
              onMouseLeave={(e) => {
                ;(e.target as HTMLButtonElement).style.borderColor = '#1a3d26'
              }}
            >
              ↺  RUN ANOTHER SCAN
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add components/AnalyzerForm.tsx
git commit -m "add AnalyzerForm: Server Action form, loading state, results reveal"
```

---

## Task 12: Home page

**Files:**
- Modify: `app/page.tsx`

- [ ] **Step 1: Replace app/page.tsx**

```typescript
// app/page.tsx
import AnalyzerForm from '@/components/AnalyzerForm'

export default function HomePage() {
  return (
    <main>
      <AnalyzerForm />
    </main>
  )
}
```

- [ ] **Step 2: Start dev server and test manually**

```bash
npm run dev
```

Open http://localhost:3000 in a browser.

Checklist:
- [ ] Dark background renders
- [ ] JetBrains Mono font loads
- [ ] Two textareas visible with green labels
- [ ] Button hover changes to green-on-dark
- [ ] Paste a short CV + short JD, click RUN DIAGNOSTIC
- [ ] Loading state shows "SCANNING..."
- [ ] Result appears with score, gaps, strengths
- [ ] RUN ANOTHER SCAN reloads

- [ ] **Step 3: Commit**

```bash
git add app/page.tsx
git commit -m "wire home page to AnalyzerForm"
```

---

## Task 13: GitHub Actions CI

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Create CI workflow**

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'

      - name: Install dependencies
        run: npm ci

      - name: Type check
        run: npx tsc --noEmit

      - name: Run tests
        run: npx vitest run

      - name: Build
        run: npm run build
```

- [ ] **Step 2: Run all tests locally to confirm they pass**

```bash
npx vitest run
```

Expected: `✓ 9 tests passed`

- [ ] **Step 3: Commit**

```bash
git add .github/
git commit -m "add GitHub Actions CI: tsc, vitest, build"
```

---

## Task 14: GitHub repo + Vercel deploy

- [ ] **Step 1: Create GitHub repo**

Go to https://github.com/new

- Name: `interview-doctor`
- Visibility: Private (can make public later)
- Do NOT initialise with README (you already have one)

Copy the remote URL shown (e.g. `https://github.com/brennoz77/interview-doctor.git`)

- [ ] **Step 2: Push to GitHub**

```bash
git remote add origin https://github.com/brennoz77/interview-doctor.git
git branch -M main
git push -u origin main
```

- [ ] **Step 3: Connect Vercel**

1. Go to https://vercel.com, sign in with GitHub
2. Click "Add New Project"
3. Select `interview-doctor` from the list
4. Framework preset: Next.js (auto-detected)
5. Click "Deploy" — first deploy will run (no env vars yet, Gemini calls will fail)

- [ ] **Step 4: Add environment variable in Vercel**

1. In Vercel dashboard → your project → Settings → Environment Variables
2. Add: `GEMINI_API_KEY` = your actual key
3. Apply to: Production, Preview, Development
4. Click Save

- [ ] **Step 5: Trigger a redeploy**

```bash
git commit --allow-empty -m "trigger redeploy with env var"
git push
```

- [ ] **Step 6: Smoke test production**

Open your Vercel URL (shown in dashboard, e.g. `interview-doctor.vercel.app`).

Checklist:
- [ ] Page loads with dark terminal theme
- [ ] Paste CV + JD, click RUN DIAGNOSTIC
- [ ] Score + gap list appear
- [ ] No console errors

---

## All Tests

Run the full test suite at any point:

```bash
npx vitest run
```

Expected: `✓ 9 tests passed` across:
- `lib/__tests__/sanitize.test.ts` — 4 tests
- `lib/__tests__/rateLimit.test.ts` — 3 tests
- `lib/__tests__/gemini.test.ts` — 1 test
- `app/actions/__tests__/analyze.test.ts` — 2 tests
