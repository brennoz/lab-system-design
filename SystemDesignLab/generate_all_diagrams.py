"""
Architecture diagram generator for SystemDesignLab projects.
Uses Chrome headless to render SVG HTML → PNG → JPEG.
Add new projects by defining a diagram_NN() function and calling generate() at the bottom.

Usage:
    python3 generate_all_diagrams.py
"""
import subprocess, os
from PIL import Image

BASE = "/home/brennoz77/LABS/LAB_CLAUDE/SystemDesignLab"

# ── Colours ───────────────────────────────────────────────────────────────────
BLUE      = "#1e40af"; BLUE_LT   = "#3b82f6"
GREEN     = "#065f46"; GREEN_LT  = "#4ade80"
RED       = "#7f1d1d"; RED_LT    = "#f87171"
PURPLE    = "#4c1d95"; PURPLE_LT = "#a78bfa"
ORANGE    = "#78350f"; ORANGE_LT = "#fb923c"
TEAL      = "#134e4a"; TEAL_LT   = "#2dd4bf"
SLATE     = "#1e293b"; SLATE_LT  = "#64748b"
TEXT      = "#e2e8f0"; MUTED     = "#94a3b8"

# ── SVG primitives ────────────────────────────────────────────────────────────
def svg_open(w, h):
    return f'''<svg width="{w}" height="{h}" xmlns="http://www.w3.org/2000/svg">
<defs>
  <marker id="arr"        markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto"><polygon points="0 0,10 3.5,0 7" fill="#475569"/></marker>
  <marker id="arr-green"  markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto"><polygon points="0 0,10 3.5,0 7" fill="#4ade80"/></marker>
  <marker id="arr-red"    markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto"><polygon points="0 0,10 3.5,0 7" fill="#f87171"/></marker>
  <marker id="arr-purple" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto"><polygon points="0 0,10 3.5,0 7" fill="#a78bfa"/></marker>
  <marker id="arr-orange" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto"><polygon points="0 0,10 3.5,0 7" fill="#fb923c"/></marker>
  <marker id="arr-teal"   markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto"><polygon points="0 0,10 3.5,0 7" fill="#2dd4bf"/></marker>
</defs>
<rect width="{w}" height="{h}" fill="#0f172a"/>
'''

def svg_close(): return '</svg>'

def t(x, y, text, anchor="middle", size=12, bold=False, color=TEXT):
    weight = "bold" if bold else "normal"
    return f'<text x="{x}" y="{y}" text-anchor="{anchor}" dominant-baseline="auto" font-family="Arial,sans-serif" font-size="{size}" font-weight="{weight}" fill="{color}">{text}</text>\n'

def box(x, y, w, h, fill, stroke, label, sub=None, lc=TEXT):
    """Simple box — label centred, optional sub-label below."""
    mid_y = y + h // 2
    label_y = mid_y - 8 if sub else mid_y + 5
    out  = f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" fill="{fill}" stroke="{stroke}" stroke-width="2"/>\n'
    out += t(x + w//2, label_y, label, size=15, bold=True, color=lc)
    if sub:
        out += t(x + w//2, mid_y + 13, sub, size=11, color=MUTED)
    return out

def service_box(x, y, w, h, fill, stroke, label, sub, items, lc=TEXT):
    """Tall box: title at top, separator, then item list — no overlap."""
    out  = f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" fill="{fill}" stroke="{stroke}" stroke-width="2"/>\n'
    out += t(x + w//2, y + 22, label, size=15, bold=True, color=lc)
    out += t(x + w//2, y + 40, sub,   size=11, color=MUTED)
    out += f'<line x1="{x+12}" y1="{y+52}" x2="{x+w-12}" y2="{y+52}" stroke="#334155" stroke-width="1"/>\n'
    for i, item in enumerate(items):
        out += t(x + w//2, y + 68 + i * 20, item, size=11, color=MUTED)
    return out

def arrow(x1, y1, x2, y2, marker="arr", color="#475569", lbl=None):
    out = f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" stroke-width="2" marker-end="url(#{marker})"/>\n'
    if lbl:
        mx, my = (x1+x2)//2, (y1+y2)//2 - 8
        out += t(mx, my, lbl, size=11, color=MUTED)
    return out

def curve(d, color="#4ade80", marker="arr-green"):
    return f'<path d="{d}" stroke="{color}" stroke-width="2" fill="none" marker-end="url(#{marker})"/>\n'

def title(w, text, sub):
    return t(w//2, 36, text, size=22, bold=True) + t(w//2, 60, sub, size=13, color=MUTED)

def section_label(x, y, text, color=GREEN_LT):
    return t(x, y, text, anchor="start", size=13, bold=True, color=color)

def note(x, y, text, color=MUTED, size=12):
    return t(x, y, text, anchor="start", size=size, color=color)

def legend(x, y, items):
    out = t(x, y, "Legend", anchor="start", size=13, bold=True, color=TEXT)
    for i, (fill, stroke, label) in enumerate(items):
        ly = y + 18 + i * 26
        out += f'<rect x="{x}" y="{ly}" width="18" height="18" rx="4" fill="{fill}" stroke="{stroke}" stroke-width="1.5"/>\n'
        out += t(x + 26, ly + 13, label, anchor="start", size=12, color=MUTED)
    return out

def notes_section(x, y, items):
    out = t(x, y, "Key Design Points", anchor="start", size=13, bold=True, color=TEXT)
    for i, n in enumerate(items):
        out += t(x, y + 20 + i * 22, n, anchor="start", size=12, color=MUTED)
    return out

def html_wrap(svg_content, w, h):
    return (f'<!DOCTYPE html><html><head><meta charset="UTF-8"/>'
            f'<style>*{{margin:0;padding:0}}body{{background:#0f172a;width:{w}px;height:{h}px;overflow:hidden}}</style>'
            f'</head><body>{svg_content}</body></html>')


# ══════════════════════════════════════════════════════════════════════════════
# 01 — Rate Limiter
# ══════════════════════════════════════════════════════════════════════════════
def diagram_01():
    W, H = 1600, 900
    s = svg_open(W, H)
    s += title(W, "01 — Rate Limiter Architecture",
               "Token Bucket · Sliding Window · Redis atomics · Per-client keys")

    # Client
    s += box(40, 200, 130, 58, SLATE, BLUE_LT, "Client", "HTTP requests", BLUE_LT)

    # Rate Limiter Service — tall box with items
    s += service_box(230, 120, 230, 200, BLUE, BLUE_LT, "Rate Limiter Service", ":8080  Spring Boot",
                     ["POST /api/v1/check", "TokenBucketUseCase", "SlidingWindowUseCase", "CheckLimitUseCase"])

    # Redis
    s += service_box(560, 120, 220, 200, ORANGE, ORANGE_LT, "Redis :6379", "Atomic counters + TTL",
                     ["INCR  token:{key}", "EXPIRE bucket TTL", "——————", "ZADD  sliding:{key} ts", "ZREMRANGEBYSCORE ...", "ZCOUNT window"])

    # Token Bucket
    s += box(880, 120, 200, 80, GREEN, GREEN_LT, "Token Bucket", "O(1) per request", GREEN_LT)
    s += note(885, 220, "Fixed capacity; refills at constant rate.", MUTED, 11)
    s += note(885, 236, "Allows short bursts up to bucket size.", MUTED, 11)

    # Sliding Window
    s += box(880, 260, 200, 80, RED, RED_LT, "Sliding Window", "O(log N) per request", RED_LT)
    s += note(885, 360, "Exact timestamp tracking in sorted set.", MUTED, 11)
    s += note(885, 376, "No burst — strict boundary accuracy.", MUTED, 11)

    # Responses
    s += box(1170, 120, 190, 58, GREEN, GREEN_LT,  "200 OK",      "request allowed",    GREEN_LT)
    s += box(1170, 260, 190, 58, RED,   RED_LT,    "429 Too Many", "Retry-After header", RED_LT)

    # Prometheus + Grafana
    s += box(230, 380, 230, 58, PURPLE, PURPLE_LT, "Prometheus :9090", "scrape /actuator/metrics", PURPLE_LT)
    s += box(230, 460, 230, 58, PURPLE, PURPLE_LT, "Grafana :3000",    "dashboards + alerts",       PURPLE_LT)

    # Arrows
    s += arrow(170, 229, 230, 229)
    s += arrow(460, 215, 560, 215, lbl="INCR / ZADD")
    s += arrow(780, 160, 880, 160, "arr-green", GREEN_LT, "bucket check")
    s += arrow(780, 270, 880, 295, "arr-red",   RED_LT,   "window check")
    s += arrow(1080, 160, 1170, 149, "arr-green", GREEN_LT)
    s += arrow(1080, 295, 1170, 289, "arr-red",   RED_LT)
    s += arrow(345, 320, 345, 380)
    s += arrow(345, 438, 345, 460)

    # Legend + Notes
    s += legend(50, 430, [
        (BLUE,   BLUE_LT,   "Spring Boot Service"),
        (GREEN,  GREEN_LT,  "Allowed path (200 OK)"),
        (RED,    RED_LT,    "Throttled path (429)"),
        (ORANGE, ORANGE_LT, "Redis counters"),
        (PURPLE, PURPLE_LT, "Observability (Prometheus + Grafana)"),
    ])
    s += notes_section(50, 600, [
        "1.  Token Bucket — fixed capacity, refills at constant rate; short bursts allowed",
        "2.  Sliding Window — tracks exact timestamps in sorted set; strict rate, no burst",
        "3.  Redis INCR + EXPIRE — atomic counter update, no Lua scripts needed",
        "4.  Per-client keys — each API key or IP is rate-limited independently",
        "5.  429 Retry-After header — RFC 6585 compliance; clients back off automatically",
    ])
    s += svg_close()
    return html_wrap(s, W, H), W, H


# ══════════════════════════════════════════════════════════════════════════════
# 05 — URL Shortener
# ══════════════════════════════════════════════════════════════════════════════
def diagram_05():
    W, H = 1600, 900
    s = svg_open(W, H)
    s += title(W, "05 — URL Shortener Architecture",
               "Base62 encoding · CQRS · Cache-Aside · Bloom Filter")

    BH = 58

    # ── WRITE PATH ─────────────────────────────────────────────────────────────
    s += section_label(50, 95, "WRITE PATH — POST /api/v1/shorten", GREEN_LT)
    xs = [50, 230, 440, 660, 880, 1110]
    bws = [150, 175, 200, 195, 205, 170]
    labels = [("Client","POST /shorten",BLUE_LT,SLATE,BLUE_LT),
              ("URL Shortener","  :8080  Spring Boot",TEXT,BLUE,BLUE_LT),
              ("PostgreSQL","INSERT → BIGSERIAL id",ORANGE_LT,ORANGE,ORANGE_LT),
              ("Base62 Encoder","id → abc123",GREEN_LT,GREEN,GREEN_LT),
              ("Bloom Filter","Redis SETBIT — mark seen",PURPLE_LT,PURPLE,PURPLE_LT),
              ("Response","shortUrl → client",GREEN_LT,GREEN,GREEN_LT)]
    for i,(lbl,sub,lc,fill,stroke) in enumerate(labels):
        s += box(xs[i], 110, bws[i], BH, fill, stroke, lbl, sub, lc)
    arrow_lbls = [None, "save URL", "encode id", "mark seen", None]
    arrow_cols = ["#475569", "#475569", GREEN_LT, PURPLE_LT, "#475569"]
    arrow_marks = ["arr","arr","arr-green","arr-purple","arr"]
    for i in range(5):
        s += arrow(xs[i]+bws[i], 139, xs[i+1], 139, arrow_marks[i], arrow_cols[i], arrow_lbls[i])

    # ── READ PATH ─────────────────────────────────────────────────────────────
    s += section_label(50, 213, "READ PATH — GET /r/{code}", RED_LT)
    s += box(50,  228, 150, BH, SLATE,  BLUE_LT,  "Client",        "GET /r/abc123",     BLUE_LT)
    s += box(240, 228, 190, BH, BLUE,   BLUE_LT,  "URL Shortener", "decode Base62 → id")
    s += box(480, 228, 185, BH, PURPLE, PURPLE_LT,"Bloom Filter",  "mightContain(url)?",PURPLE_LT)
    s += box(720, 228, 165, BH, RED,    RED_LT,   "404 Not Found", "URL never existed", RED_LT)

    s += arrow(200, 257, 240, 257)
    s += arrow(430, 257, 480, 257)
    s += arrow(665, 257, 720, 257, "arr-red", RED_LT, "not seen → 404")

    # Bloom → cache
    s += box(480, 340, 185, BH, ORANGE, ORANGE_LT,"Redis Cache",  "Cache-Aside lookup", ORANGE_LT)
    s += box(720, 340, 165, BH, GREEN,  GREEN_LT, "302 Redirect", "cache hit",          GREEN_LT)
    s += arrow(572, 286, 572, 340, "arr-orange", ORANGE_LT, "seen → check cache")
    s += arrow(665, 369, 720, 369, "arr-green", GREEN_LT, "hit → redirect")

    # cache miss → postgres
    s += box(480, 450, 185, BH, ORANGE, ORANGE_LT,"PostgreSQL",   "SELECT by id",       ORANGE_LT)
    s += box(720, 450, 165, BH, GREEN,  GREEN_LT, "302 Redirect", "write cache + return",GREEN_LT)
    s += arrow(572, 398, 572, 450, lbl="cache miss")
    s += arrow(665, 479, 720, 479, "arr-green", GREEN_LT)
    s += arrow(803, 450, 803, 398, "arr-orange", ORANGE_LT)
    s += note(810, 426, "write-through", MUTED, 10)

    # Legend + Notes
    s += legend(950, 310, [
        (BLUE,   BLUE_LT,   "Spring Boot Service"),
        (PURPLE, PURPLE_LT, "Bloom Filter (dedup gate)"),
        (GREEN,  GREEN_LT,  "Success path (200 / 302)"),
        (RED,    RED_LT,    "Error path (404)"),
        (ORANGE, ORANGE_LT, "Storage (PostgreSQL / Redis)"),
    ])
    s += notes_section(50, 580, [
        "1.  Base62 (not Base64) — URL-safe alphabet 0-9a-zA-Z; no +/= needing percent-encoding",
        "2.  Auto-increment ID → Base62 encode — zero collision risk unlike hashing approaches",
        "3.  Bloom Filter gates the read path — O(1) Redis bit check before any DB query",
        "4.  Cache-Aside — app manages Redis; on miss: fetch DB, populate cache, then redirect",
        "5.  CQRS — ShortenUrlUseCase (write) and ResolveUrlUseCase (read) are fully separate",
    ])
    s += svg_close()
    return html_wrap(s, W, H), W, H


# ══════════════════════════════════════════════════════════════════════════════
# 06 — Web Crawler
# ══════════════════════════════════════════════════════════════════════════════
def diagram_06():
    W, H = 1600, 900
    s = svg_open(W, H)
    s += title(W, "06 — Web Crawler Architecture",
               "BFS graph traversal · Bloom Filter dedup · Politeness delay · RabbitMQ work queue")

    BW, BH = 175, 58

    # Client
    s += box(30, 155, 135, BH, SLATE, BLUE_LT, "Client", "POST /seeds", BLUE_LT)

    # Web Crawler Service
    s += service_box(220, 90, 225, 190, BLUE, BLUE_LT, "Web Crawler Service", ":8081  Spring Boot",
                     ["SubmitSeedUseCase", "CrawlUrlUseCase", "UrlNormaliser", "LinkExtractor (Jsoup)"])

    # Bloom Filter
    s += box(530, 95,  BW, BH, PURPLE, PURPLE_LT, "Bloom Filter",   "Redis SETBIT/GETBIT", PURPLE_LT)
    # seen → skip
    s += box(780, 95,  BW, BH, SLATE,  SLATE_LT,  "seen → skip",    "URL already crawled", SLATE_LT)
    # RabbitMQ
    s += box(530, 195, BW, BH, RED,    RED_LT,    "RabbitMQ",       "url.frontier queue",  RED_LT)
    # Politeness
    s += box(530, 295, BW, BH, ORANGE, ORANGE_LT, "Politeness Lock","Redis SET EX 1s",     ORANGE_LT)

    # Worker
    s += service_box(780, 155, BW+15, 130, BLUE, BLUE_LT, "CrawlUrlUseCase", "@RabbitListener",
                     ["isLocked? → re-enqueue", "politeness.lock(domain)", "fetcher.fetch(url)"])

    # HTTP Fetcher
    s += box(780, 315, BW+15, BH, TEAL,   TEAL_LT,  "HTTP Fetcher",   "RestTemplate + User-Agent", TEAL_LT)
    # Link Extractor
    s += box(780, 405, BW+15, BH, TEAL,   TEAL_LT,  "Link Extractor", "Jsoup parse &lt;a href&gt;",   TEAL_LT)
    # URL Normaliser
    s += box(780, 495, BW+15, BH, TEAL,   TEAL_LT,  "URL Normaliser", "strip utm_* fragment",     TEAL_LT)

    # External Web
    s += box(1060, 315, BW-10, BH, SLATE,  SLATE_LT, "External Web",  "wikipedia.org, bbc.com…",  SLATE_LT)
    # PostgreSQL
    s += box(1060, 175, BW-10, 80, ORANGE, ORANGE_LT,"PostgreSQL",    "crawled_pages table",      ORANGE_LT)
    # Depth Guard
    s += box(1060, 495, BW-10, BH, GREEN,  GREEN_LT, "Depth Guard",   "MAX_DEPTH = 3",            GREEN_LT)

    # Arrows
    s += arrow(165, 184, 220, 184)
    s += arrow(445, 124, 530, 124, "arr-purple", PURPLE_LT, "mightContain?")
    s += arrow(705, 124, 780, 124, "arr-purple", PURPLE_LT, "yes → skip")
    s += arrow(445, 224, 530, 224, "arr-red",    RED_LT,    "enqueue")
    s += arrow(705, 224, 780, 204)
    s += arrow(617, 165, 617, 195)                          # bloom → queue
    s += arrow(617, 295, 870, 285, "arr-orange", ORANGE_LT, "isLocked?")
    s += arrow(872, 315, 872, 315)
    s += arrow(872, 285, 872, 315)
    s += arrow(987, 220, 1060, 215)                         # worker → postgres
    s += arrow(987, 344, 1060, 344)                         # fetcher → external web
    s += arrow(872, 373, 872, 405)                          # fetcher → extractor
    s += arrow(872, 463, 872, 495)                          # extractor → normaliser
    s += arrow(987, 524, 1060, 524)                         # normaliser → depth guard
    s += curve("M872,553 C872,600 590,600 617,258", GREEN_LT, "arr-green")
    s += note(660, 595, "re-enqueue unseen child links (depth + 1)", GREEN_LT, 11)

    s += legend(50, 420, [
        (BLUE,   BLUE_LT,   "Spring Boot Service / worker"),
        (PURPLE, PURPLE_LT, "Bloom Filter (seen URL dedup)"),
        (RED,    RED_LT,    "RabbitMQ work queue"),
        (ORANGE, ORANGE_LT, "Redis / PostgreSQL storage"),
        (TEAL,   TEAL_LT,   "Domain services (fetch / parse / normalise)"),
        (GREEN,  GREEN_LT,  "Depth guard + re-enqueue loop"),
    ])
    s += notes_section(50, 640, [
        "1.  BFS over directed web graph — finds high-value pages before deep obscure ones",
        "2.  Bloom Filter (Redis SETBIT/GETBIT) — O(1) dedup; shared across all crawler instances",
        "3.  Politeness lock — Redis SET EX 1s per domain; prevents hammering a single host",
        "4.  RabbitMQ work queue — consume-and-discard; no replay needed (unlike Kafka)",
        "5.  MAX_DEPTH=3 — prevents crawler traps (session IDs, calendars, infinite param URLs)",
    ])
    s += svg_close()
    return html_wrap(s, W, H), W, H


# ══════════════════════════════════════════════════════════════════════════════
# 07 — Notification System
# ══════════════════════════════════════════════════════════════════════════════
def diagram_07():
    W, H = 1600, 900
    s = svg_open(W, H)
    s += title(W, "07 — Notification System Architecture",
               "Fan-out on Write · Two-Lane Priority Queues · Strategy Pattern (Channels) · Idempotency")

    BW, BH = 165, 58

    # Client
    s += box(30, 185, 130, BH, SLATE, BLUE_LT, "Client", "POST /send", BLUE_LT)

    # Notification Service
    s += service_box(215, 100, 230, 210, BLUE, BLUE_LT, "Notification Service", ":8082  Spring Boot",
                     ["SendNotificationUseCase", "ProcessNotificationTask", "FanOutService", "PreferencePort check"])

    # Preferences DB
    s += box(215, 360, 230, BH, ORANGE, ORANGE_LT, "Preferences DB", "PostgreSQL — opt-out", ORANGE_LT)

    # Queues
    s += box(545, 120, BW+10, BH, RED,   RED_LT,   "notifications.critical", "RabbitMQ — CRITICAL", RED_LT)
    s += box(545, 215, BW+10, BH, GREEN, GREEN_LT, "notifications.bulk",     "RabbitMQ — BULK",     GREEN_LT)

    # Workers
    s += box(820, 120, BW, BH, RED,   RED_LT,   "Critical Worker", "fast consumer", RED_LT)
    s += box(820, 215, BW, BH, GREEN, GREEN_LT, "Bulk Worker",     "rate-limited",  GREEN_LT)

    # Channels
    s += box(1090, 90,  BW, BH, PURPLE, PURPLE_LT,"Email Channel",  "WireMock :8089", PURPLE_LT)
    s += box(1090, 185, BW, BH, PURPLE, PURPLE_LT,"SMS Channel",    "WireMock :8089", PURPLE_LT)
    s += box(1090, 280, BW, BH, ORANGE, ORANGE_LT,"In-App Channel", "PostgreSQL",     ORANGE_LT)

    # WireMock + PostgreSQL (right column)
    s += box(1355, 120, BW-10, BH, PURPLE, PURPLE_LT,"WireMock :8089",  "simulates providers",   PURPLE_LT)
    s += box(1355, 255, BW-10, BH, ORANGE, ORANGE_LT,"PostgreSQL",      "notifications table",   ORANGE_LT)

    # Idempotency
    s += box(820, 335, BW+30, 50, SLATE, SLATE_LT, "Idempotency Check", "UUID → skip if SENT", SLATE_LT)

    # Arrows
    s += arrow(160, 214, 215, 200)
    s += arrow(445, 149, 545, 149, "arr-red",   RED_LT,   "CRITICAL")
    s += arrow(445, 244, 545, 244, "arr-green", GREEN_LT, "BULK")
    s += arrow(330, 310, 330, 360)
    s += note(338, 345, "check opt-out", MUTED, 10)
    s += arrow(720, 149, 820, 149)
    s += arrow(720, 244, 820, 244)

    # Workers → channels
    s += arrow(985, 140, 1090, 119, "arr-red",   RED_LT)
    s += arrow(985, 155, 1090, 210, "arr-green", GREEN_LT)
    s += arrow(985, 255, 1090, 309, "arr-green", GREEN_LT)

    # Channels → WireMock / PostgreSQL
    s += arrow(1255, 120, 1355, 150)
    s += arrow(1255, 210, 1355, 152)
    s += arrow(1255, 309, 1355, 285, "arr-orange", ORANGE_LT)

    # Worker → idempotency check
    s += arrow(902, 273, 902, 335)
    s += note(780, 400, "UUID per task — prevents double delivery on RabbitMQ retry", MUTED, 11)

    s += legend(50, 450, [
        (BLUE,   BLUE_LT,   "Spring Boot Service"),
        (RED,    RED_LT,    "CRITICAL priority path"),
        (GREEN,  GREEN_LT,  "BULK priority path"),
        (PURPLE, PURPLE_LT, "External channel (WireMock)"),
        (ORANGE, ORANGE_LT, "PostgreSQL storage"),
    ])
    s += notes_section(50, 640, [
        "1.  Fan-out on write — one task per recipient enqueued immediately; delivery SLA over storage cost",
        "2.  Two separate queues — CRITICAL never waits behind BULK; consumers scale independently",
        "3.  Strategy pattern — ChannelPort per channel; add new channel without touching the use case",
        "4.  Idempotency UUID — worker checks DB before send; safe on RabbitMQ at-least-once redelivery",
        "5.  Preference check before fan-out — opted-out recipients never consume a queue slot",
    ])
    s += svg_close()
    return html_wrap(s, W, H), W, H


# ══════════════════════════════════════════════════════════════════════════════
# Runner
# ══════════════════════════════════════════════════════════════════════════════
def generate(project_dir, diagram_fn):
    html, w, h = diagram_fn()
    html_path = os.path.join(project_dir, "_diagram_tmp.html")
    jpg_path  = os.path.join(project_dir, "architecture.jpg")

    with open(html_path, "w") as f:
        f.write(html)

    subprocess.run([
        "google-chrome", "--headless", "--disable-gpu", "--no-sandbox",
        "--screenshot", f"--window-size={w},{h}",
        f"file://{html_path}"
    ], cwd=project_dir, capture_output=True)

    img = Image.open(os.path.join(project_dir, "screenshot.png"))
    img.save(jpg_path, "JPEG", quality=95)
    os.remove(os.path.join(project_dir, "screenshot.png"))
    os.remove(html_path)
    print(f"OK: {jpg_path}")


if __name__ == "__main__":
    generate(f"{BASE}/01-rate-limiter",        diagram_01)
    generate(f"{BASE}/05-url-shortener",       diagram_05)
    generate(f"{BASE}/06-web-crawler",         diagram_06)
    generate(f"{BASE}/07-notification-system", diagram_07)
