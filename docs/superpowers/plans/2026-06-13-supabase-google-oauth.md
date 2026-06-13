# Supabase + Google OAuth Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Google OAuth via Supabase to InterviewDoctor — a "Sign in with Google" button in the nav, user email when logged in, sign out, session persistence via middleware, and a `profiles` table as the foundation for all future auth-gated features.

**Architecture:** `@supabase/ssr` manages separate server-side and browser-side Supabase clients. A Next.js middleware refreshes the session on every request to prevent expiry. A Server Component Nav reads the user server-side and passes it down to a Client Component AuthButton — no client-side flash of unauthenticated content. An OAuth callback route exchanges the authorization code for a session.

**Tech Stack:** Next.js 14 App Router, `@supabase/ssr`, `@supabase/supabase-js`, `@testing-library/react`, Vitest + jsdom.

---

## File Structure

| File | Action | Purpose |
|---|---|---|
| `lib/supabase/server.ts` | Create | Server-side Supabase client (Server Components, Server Actions) |
| `lib/supabase/client.ts` | Create | Browser-side Supabase client (Client Components) |
| `middleware.ts` | Create | Refresh Supabase session token on every request |
| `app/auth/callback/route.ts` | Create | Exchange OAuth authorization code for session |
| `components/AuthButton.tsx` | Create | Client Component: "Sign in with Google" or email + sign out |
| `components/Nav.tsx` | Create | Server Component: reads user, renders AuthButton |
| `components/__tests__/AuthButton.test.tsx` | Create | Unit tests for AuthButton (2 tests) |
| `app/layout.tsx` | Modify | Add `<Nav />` above `{children}` |
| `components/AnalyzerForm.tsx` | Modify | Remove duplicate INTERVIEW·DOCTOR header (now in Nav) |
| `.env.example` | Modify | Document Supabase env vars |

---

## Prerequisites (manual — do these before writing any code)

- [ ] Create Supabase project at https://supabase.com (free tier, no card required)
- [ ] In Supabase: Authentication → Providers → Google → Enable
- [ ] In Google Cloud Console (https://console.cloud.google.com): create OAuth 2.0 credentials
  - Authorized redirect URI: `https://<your-project-ref>.supabase.co/auth/v1/callback`
- [ ] In Supabase: Authentication → URL Configuration
  - Site URL: `https://interview-doctor.vercel.app`
  - Redirect URLs: `https://interview-doctor.vercel.app/auth/callback` and `http://localhost:3000/auth/callback`
- [ ] Copy values from Supabase → Settings → API into `.env.local`:
  ```
  NEXT_PUBLIC_SUPABASE_URL=https://your-project-ref.supabase.co
  NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key
  ```
- [ ] Run this SQL in Supabase → SQL Editor to create the profiles table:
  ```sql
  create table profiles (
    id uuid references auth.users primary key,
    display_name text,
    avatar_url text,
    created_at timestamptz default now()
  );

  alter table profiles enable row level security;

  create policy "Users can view their own profile"
    on profiles for select using (auth.uid() = id);

  create policy "Users can insert their own profile"
    on profiles for insert with check (auth.uid() = id);
  ```

---

## Task 1: Install packages and update env example

**Files:**
- Modify: `.env.example`
- Run: `npm install`

- [ ] **Step 1: Install Supabase packages**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/InterviewDoctor
npm install @supabase/ssr @supabase/supabase-js
```

Expected: packages added to `package.json`, no errors.

- [ ] **Step 2: Install testing-library for component tests**

```bash
npm install --save-dev @testing-library/react @testing-library/dom
```

Expected: packages added to `devDependencies`, no errors.

- [ ] **Step 3: Update `.env.example`**

Replace the full contents of `.env.example` with:

```
# Copy this file to .env.local and fill in the values

# Get a free key at https://console.groq.com (free tier works in UK/EU)
GROQ_API_KEY=

# Supabase — create a free project at https://supabase.com
NEXT_PUBLIC_SUPABASE_URL=
NEXT_PUBLIC_SUPABASE_ANON_KEY=
```

- [ ] **Step 4: Commit**

```bash
git add package.json package-lock.json .env.example
git commit -m "feat: install @supabase/ssr, @supabase/supabase-js, @testing-library/react"
```

---

## Task 2: Supabase server client

**Files:**
- Create: `lib/supabase/server.ts`

No unit test: `cookies()` from `next/headers` requires the Next.js runtime — impossible to unit test without a full server context.

- [ ] **Step 1: Create `lib/supabase/server.ts`**

```typescript
import { createServerClient } from '@supabase/ssr'
import { cookies } from 'next/headers'

// Pattern: Adapter — wraps @supabase/ssr with Next.js App Router cookie handling
// Why: @supabase/ssr needs to read/write cookies to persist the session across requests
export function createClient() {
  const cookieStore = cookies()

  return createServerClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      cookies: {
        getAll() {
          return cookieStore.getAll()
        },
        setAll(cookiesToSet) {
          try {
            cookiesToSet.forEach(({ name, value, options }) =>
              cookieStore.set(name, value, options)
            )
          } catch {
            // Called from a Server Component — cookie mutation handled by middleware instead
          }
        },
      },
    }
  )
}
```

- [ ] **Step 2: Verify TypeScript compiles**

```bash
npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add lib/supabase/server.ts
git commit -m "feat: add Supabase server client"
```

---

## Task 3: Supabase browser client

**Files:**
- Create: `lib/supabase/client.ts`

- [ ] **Step 1: Create `lib/supabase/client.ts`**

```typescript
import { createBrowserClient } from '@supabase/ssr'

// Pattern: Factory — create one client per Client Component, shares session from cookies
// Why separate from server.ts: browser client uses document.cookie, server client uses next/headers
export function createClient() {
  return createBrowserClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  )
}
```

- [ ] **Step 2: Verify TypeScript compiles**

```bash
npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add lib/supabase/client.ts
git commit -m "feat: add Supabase browser client"
```

---

## Task 4: Session refresh middleware

**Files:**
- Create: `middleware.ts`

- [ ] **Step 1: Create `middleware.ts`**

```typescript
import { createServerClient } from '@supabase/ssr'
import { NextResponse, type NextRequest } from 'next/server'

// Why middleware: Supabase session tokens expire; middleware refreshes them on every request
// so Server Components always receive a valid session without explicit re-login
export async function middleware(request: NextRequest) {
  let supabaseResponse = NextResponse.next({ request })

  const supabase = createServerClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!,
    {
      cookies: {
        getAll() {
          return request.cookies.getAll()
        },
        setAll(cookiesToSet) {
          cookiesToSet.forEach(({ name, value }) => request.cookies.set(name, value))
          supabaseResponse = NextResponse.next({ request })
          cookiesToSet.forEach(({ name, value, options }) =>
            supabaseResponse.cookies.set(name, value, options)
          )
        },
      },
    }
  )

  // Do not remove — refreshes session token and writes updated cookies to response
  await supabase.auth.getUser()

  return supabaseResponse
}

export const config = {
  matcher: [
    '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
  ],
}
```

- [ ] **Step 2: Verify TypeScript compiles**

```bash
npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add middleware.ts
git commit -m "feat: add Supabase session refresh middleware"
```

---

## Task 5: OAuth callback route

**Files:**
- Create: `app/auth/callback/route.ts`

Google → Supabase → redirects to `/auth/callback?code=xxx`. This route exchanges the code for a session and redirects home.

- [ ] **Step 1: Create `app/auth/callback/route.ts`**

```typescript
import { NextResponse } from 'next/server'
import { createClient } from '@/lib/supabase/server'

export async function GET(request: Request) {
  const { searchParams, origin } = new URL(request.url)
  const code = searchParams.get('code')
  const next = searchParams.get('next') ?? '/'

  if (code) {
    const supabase = createClient()
    const { error } = await supabase.auth.exchangeCodeForSession(code)
    if (!error) {
      return NextResponse.redirect(`${origin}${next}`)
    }
  }

  // Code missing or exchange failed — redirect home with error flag
  return NextResponse.redirect(`${origin}/?error=auth`)
}
```

- [ ] **Step 2: Verify TypeScript compiles**

```bash
npx tsc --noEmit
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add app/auth/callback/route.ts
git commit -m "feat: add OAuth callback route"
```

---

## Task 6: AuthButton component (TDD)

**Files:**
- Create: `components/__tests__/AuthButton.test.tsx`
- Create: `components/AuthButton.tsx`

AuthButton receives `user` as a prop from Nav (server-rendered). Renders sign-in button or email + sign-out based on prop — no async calls in render path.

- [ ] **Step 1: Write the failing tests**

Create `components/__tests__/AuthButton.test.tsx`:

```typescript
import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import type { User } from '@supabase/supabase-js'

vi.mock('@/lib/supabase/client', () => ({
  createClient: vi.fn().mockReturnValue({
    auth: {
      signInWithOAuth: vi.fn(),
      signOut: vi.fn(),
    },
  }),
}))

vi.mock('next/navigation', () => ({
  useRouter: vi.fn().mockReturnValue({ refresh: vi.fn() }),
}))

import AuthButton from '@/components/AuthButton'

const mockUser = {
  id: 'test-id',
  email: 'test@example.com',
  aud: 'authenticated',
  role: 'authenticated',
  created_at: '2024-01-01',
  app_metadata: {},
  user_metadata: {},
} as User

describe('AuthButton', () => {
  it('shows sign in button when user is null', () => {
    render(<AuthButton user={null} />)
    expect(screen.getByText(/SIGN IN WITH GOOGLE/i)).toBeTruthy()
  })

  it('shows email and sign out button when user is present', () => {
    render(<AuthButton user={mockUser} />)
    expect(screen.getByText('test@example.com')).toBeTruthy()
    expect(screen.getByText(/SIGN OUT/i)).toBeTruthy()
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

```bash
npx vitest run components/__tests__/AuthButton.test.tsx
```

Expected: FAIL — `Cannot find module '@/components/AuthButton'`

- [ ] **Step 3: Implement `components/AuthButton.tsx`**

```typescript
'use client'

import type { User } from '@supabase/supabase-js'
import { createClient } from '@/lib/supabase/client'
import { useRouter } from 'next/navigation'

type Props = { user: User | null }

export default function AuthButton({ user }: Props) {
  const supabase = createClient()
  const router = useRouter()

  async function signIn() {
    await supabase.auth.signInWithOAuth({
      provider: 'google',
      options: { redirectTo: `${location.origin}/auth/callback` },
    })
  }

  async function signOut() {
    await supabase.auth.signOut()
    router.refresh()
  }

  if (user) {
    return (
      <div className="flex items-center gap-4">
        <span className="text-xs" style={{ color: '#7aad8e' }}>
          {user.email}
        </span>
        <button
          onClick={signOut}
          className="text-xs tracking-widest border px-4 py-2 transition-colors"
          style={{ borderColor: '#1a3d26', color: '#7aad8e' }}
          onMouseEnter={(e) => ((e.target as HTMLButtonElement).style.borderColor = '#7aad8e')}
          onMouseLeave={(e) => ((e.target as HTMLButtonElement).style.borderColor = '#1a3d26')}
        >
          SIGN OUT
        </button>
      </div>
    )
  }

  return (
    <button
      onClick={signIn}
      className="text-xs tracking-widest border px-4 py-2 transition-colors"
      style={{ borderColor: '#00e87a', color: '#00e87a' }}
      onMouseEnter={(e) => {
        ;(e.target as HTMLButtonElement).style.backgroundColor = '#00e87a'
        ;(e.target as HTMLButtonElement).style.color = '#030d07'
      }}
      onMouseLeave={(e) => {
        ;(e.target as HTMLButtonElement).style.backgroundColor = 'transparent'
        ;(e.target as HTMLButtonElement).style.color = '#00e87a'
      }}
    >
      ⬡ SIGN IN WITH GOOGLE
    </button>
  )
}
```

- [ ] **Step 4: Run AuthButton tests**

```bash
npx vitest run components/__tests__/AuthButton.test.tsx
```

Expected: 2 tests PASS

- [ ] **Step 5: Run all tests**

```bash
npx vitest run
```

Expected: 12 tests PASS (10 existing + 2 new)

- [ ] **Step 6: Commit**

```bash
git add components/AuthButton.tsx components/__tests__/AuthButton.test.tsx
git commit -m "feat: add AuthButton component (TDD) — Google sign in / sign out"
```

---

## Task 7: Nav component + wire into layout

**Files:**
- Create: `components/Nav.tsx`
- Modify: `app/layout.tsx`
- Modify: `components/AnalyzerForm.tsx`

Nav is a Server Component — reads user once per request server-side, passes to AuthButton as prop. Avoids any flash of unauthenticated UI.

- [ ] **Step 1: Create `components/Nav.tsx`**

```typescript
import { createClient } from '@/lib/supabase/server'
import AuthButton from './AuthButton'

// Server Component — user read server-side, no client auth state needed
export default async function Nav() {
  const supabase = createClient()
  const {
    data: { user },
  } = await supabase.auth.getUser()

  return (
    <nav
      className="flex items-center justify-between px-6 py-4 border-b"
      style={{ borderColor: '#1a3d26' }}
    >
      <div className="text-xs tracking-widest" style={{ color: '#00e87a' }}>
        INTERVIEW · DOCTOR
      </div>
      <AuthButton user={user} />
    </nav>
  )
}
```

- [ ] **Step 2: Modify `app/layout.tsx` to add Nav**

Replace the full contents of `app/layout.tsx` with:

```typescript
import type { Metadata } from 'next'
import { JetBrains_Mono } from 'next/font/google'
import { Analytics } from '@vercel/analytics/react'
import Nav from '@/components/Nav'
import './globals.css'

const jetbrainsMono = JetBrains_Mono({
  subsets: ['latin'],
  variable: '--font-mono',
  display: 'swap',
})

export const metadata: Metadata = {
  title: 'InterviewDoctor — Diagnose your interview readiness',
  description:
    'AI-powered CV analyser that scores your readiness for a job, surfaces skill gaps and strengths, and generates a personalised study plan based on your available time.',
  keywords: ['interview preparation', 'CV analyser', 'job readiness', 'skill gap analysis', 'AI career tool'],
  metadataBase: new URL('https://interview-doctor.vercel.app'),
  openGraph: {
    title: 'InterviewDoctor — Diagnose your interview readiness',
    description:
      'Paste your CV and a job description. Get an instant AI diagnosis: readiness score, skill gaps, and confirmed strengths.',
    url: 'https://interview-doctor.vercel.app',
    siteName: 'InterviewDoctor',
    type: 'website',
  },
  twitter: {
    card: 'summary',
    title: 'InterviewDoctor — Diagnose your interview readiness',
    description:
      'Paste your CV and a job description. Get an instant AI diagnosis: readiness score, skill gaps, and confirmed strengths.',
  },
  robots: {
    index: true,
    follow: true,
  },
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className={jetbrainsMono.variable}>
      <body className="font-mono min-h-screen bg-[#030d07] text-[#d4f0e0] antialiased">
        <Nav />
        {children}
        <Analytics />
      </body>
    </html>
  )
}
```

- [ ] **Step 3: Remove duplicate header from AnalyzerForm**

In `components/AnalyzerForm.tsx`, delete lines 41–48 (the INTERVIEW·DOCTOR header block):

```typescript
        <div
          className="text-xs tracking-widest mb-3 flex items-center gap-2"
          style={{ color: '#00e87a' }}
        >
          <span>INTERVIEW</span>
          <span style={{ color: '#1a3d26' }}>●</span>
          <span>DOCTOR</span>
        </div>
```

- [ ] **Step 4: Run all tests**

```bash
npx vitest run
```

Expected: 12 tests PASS

- [ ] **Step 5: TypeScript check**

```bash
npx tsc --noEmit
```

Expected: no errors

- [ ] **Step 6: Start dev server and verify manually**

```bash
npm run dev
```

Open http://localhost:3000 and verify:
- Nav at top: "INTERVIEW · DOCTOR" on left, "⬡ SIGN IN WITH GOOGLE" on right
- Clicking sign in redirects to Google OAuth
- After sign in: nav shows email + "SIGN OUT" button
- Clicking sign out clears session and shows sign in button again
- No duplicate INTERVIEW·DOCTOR header on the page

- [ ] **Step 7: Add Supabase env vars to Vercel**

In Vercel dashboard → interview-doctor → Settings → Environment Variables, add:
- `NEXT_PUBLIC_SUPABASE_URL` = your Supabase project URL
- `NEXT_PUBLIC_SUPABASE_ANON_KEY` = your Supabase anon key

- [ ] **Step 8: Commit and push**

```bash
git add components/Nav.tsx components/AnalyzerForm.tsx app/layout.tsx
git commit -m "feat: add Nav with Google OAuth, wire into layout"
git push
```
