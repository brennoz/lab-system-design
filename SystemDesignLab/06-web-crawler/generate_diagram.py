from PIL import Image, ImageDraw, ImageFont
import math

W, H = 1600, 820
BG        = "#0f172a"
BLUE      = "#1e40af"; BLUE_LT   = "#3b82f6"
GREEN     = "#065f46"; GREEN_LT  = "#4ade80"
RED       = "#7f1d1d"; RED_LT    = "#f87171"
PURPLE    = "#4c1d95"; PURPLE_LT = "#a78bfa"
ORANGE    = "#78350f"; ORANGE_LT = "#fb923c"
TEAL      = "#134e4a"; TEAL_LT   = "#2dd4bf"
SLATE     = "#1e293b"
TEXT      = "#e2e8f0"; MUTED     = "#94a3b8"; ARROW = "#475569"

img  = Image.new("RGB", (W, H), BG)
draw = ImageDraw.Draw(img)

def rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i+2], 16) for i in (0,2,4))

def font(size, bold=False):
    name = "DejaVuSans-Bold.ttf" if bold else "DejaVuSans.ttf"
    try:    return ImageFont.truetype(f"/usr/share/fonts/truetype/dejavu/{name}", size)
    except: return ImageFont.load_default()

def box(x, y, w, h, fill, border, label, sub=None, lc=TEXT):
    draw.rounded_rectangle([x,y,x+w,y+h], radius=10, fill=rgb(fill), outline=rgb(border), width=2)
    cx = x + w//2
    if sub:
        draw.text((cx, y+h//2-11), label, fill=rgb(lc),   font=font(15,True), anchor="mm")
        draw.text((cx, y+h//2+10), sub,   fill=rgb(MUTED), font=font(11),      anchor="mm")
    else:
        draw.text((cx, y+h//2), label, fill=rgb(lc), font=font(15,True), anchor="mm")

def arrow(x1,y1,x2,y2,c=ARROW,lbl=None):
    draw.line([x1,y1,x2,y2], fill=rgb(c), width=2)
    a = math.atan2(y2-y1, x2-x1); s=10
    draw.polygon([(x2,y2),(x2-s*math.cos(a-0.4),y2-s*math.sin(a-0.4)),(x2-s*math.cos(a+0.4),y2-s*math.sin(a+0.4))],fill=rgb(c))
    if lbl:
        mx,my = (x1+x2)//2,(y1+y2)//2
        draw.text((mx,my-10), lbl, fill=rgb(MUTED), font=font(11), anchor="mm")

# Title
draw.text((W//2,34), "06 — Web Crawler Architecture", fill=rgb(TEXT), font=font(22,True), anchor="mm")
draw.text((W//2,58), "BFS graph traversal · Bloom Filter dedup · Politeness delay · RabbitMQ work queue", fill=rgb(MUTED), font=font(13), anchor="mm")

# ── Boxes ─────────────────────────────────────────────────────────────────────
# Client
box(30, 140, 140, 60, SLATE, BLUE_LT, "Client", "POST /seeds", BLUE_LT)

# Web Crawler Service
box(230, 90, 220, 160, BLUE, BLUE_LT, "Web Crawler Service", ":8081 Spring Boot", TEXT)
draw.text((340,185), "SubmitSeedUseCase",  fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((340,203), "CrawlUrlUseCase",    fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((340,221), "UrlNormaliser",      fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((340,239), "LinkExtractor(Jsoup)", fill=rgb(MUTED), font=font(11), anchor="mm")

# Bloom Filter (Redis)
box(530, 90, 180, 60, PURPLE, PURPLE_LT, "Bloom Filter", "Redis SETBIT/GETBIT", PURPLE_LT)

# RabbitMQ
box(530, 190, 180, 60, RED, RED_LT, "RabbitMQ", "url.frontier queue", RED_LT)

# Politeness (Redis)
box(530, 290, 180, 60, ORANGE, ORANGE_LT, "Politeness Lock", "Redis SET EX 1s", ORANGE_LT)

# CrawlUrlUseCase Consumer
box(790, 170, 200, 80, BLUE, BLUE_LT, "CrawlUrlUseCase", "@RabbitListener consumer", TEXT)

# HTTP Fetcher
box(790, 290, 200, 60, TEAL, TEAL_LT, "HTTP Fetcher", "RestTemplate + User-Agent", TEAL_LT)

# Link Extractor
box(790, 390, 200, 60, TEAL, TEAL_LT, "Link Extractor", "Jsoup — parse <a href>", TEAL_LT)

# URL Normaliser
box(790, 490, 200, 60, TEAL, TEAL_LT, "URL Normaliser", "strip utm_* fragment", TEAL_LT)

# External Web
box(1070,290, 180, 60, SLATE, MUTED, "External Web", "wikipedia.org, bbc.com …", MUTED)

# PostgreSQL
box(1070,170, 180, 80, ORANGE, ORANGE_LT, "PostgreSQL", "crawled_pages table", ORANGE_LT)

# Depth guard
box(1070,490, 180, 60, GREEN, GREEN_LT, "Depth Guard", "MAX_DEPTH = 3", GREEN_LT)

# ── Arrows ─────────────────────────────────────────────────────────────────────
arrow(170, 170, 230, 170)                              # Client → Service
arrow(450, 120, 530, 120, PURPLE_LT, "mightContain?") # Service → Bloom
arrow(450, 200, 530, 210, RED_LT,    "enqueue")        # Service → RabbitMQ
arrow(710, 220, 790, 210)                              # RabbitMQ → Worker
arrow(710, 320, 790, 320, ORANGE_LT, "isLocked?")     # Politeness → Worker
arrow(790, 320, 790, 290)                              # Worker → isLocked check (up)
arrow(990, 210, 1070,210)                              # Worker → PostgreSQL
arrow(990, 320, 1070,320)                              # Fetcher → External Web
arrow(990, 420, 620, 210, GREEN_LT)                   # LinkExtractor → re-enqueue
arrow(990, 520, 1070,520)                              # Normaliser → Depth Guard
arrow(880, 250, 880, 290)                              # Worker → Fetcher
arrow(880, 350, 880, 390)                              # Fetcher → LinkExtractor
arrow(880, 450, 880, 490)                              # LinkExtractor → Normaliser

# Bloom → skip
arrow(620, 90, 730, 75, PURPLE_LT)
draw.text((680, 70), "seen → skip", fill=rgb(PURPLE_LT), font=font(11), anchor="mm")

# ── Legend ─────────────────────────────────────────────────────────────────────
LX, LY = 50, 440
draw.text((LX, LY), "Legend", fill=rgb(TEXT), font=font(13,True))
items = [
    (BLUE,   BLUE_LT,   "Spring Boot Service / worker"),
    (PURPLE, PURPLE_LT, "Bloom Filter (URL dedup)"),
    (RED,    RED_LT,    "RabbitMQ work queue"),
    (ORANGE, ORANGE_LT, "Redis / PostgreSQL storage"),
    (TEAL,   TEAL_LT,   "Domain services (fetcher, parser, normaliser)"),
    (GREEN,  GREEN_LT,  "Depth guard (MAX_DEPTH=3)"),
]
for i,(f,b,t) in enumerate(items):
    draw.rounded_rectangle([LX,LY+20+i*26,LX+18,LY+38+i*26], radius=3, fill=rgb(f), outline=rgb(b), width=1)
    draw.text((LX+26, LY+29+i*26), t, fill=rgb(MUTED), font=font(12), anchor="lm")

# Key notes
NX = 50
draw.text((NX, 620), "Key Design Points", fill=rgb(TEXT), font=font(13,True))
notes = [
    "1.  BFS over directed web graph — finds high-value pages before deep obscure ones",
    "2.  Bloom Filter (Redis SETBIT/GETBIT) — O(1) dedup; shared across all crawler instances",
    "3.  Politeness lock — Redis SET EX 1s per domain; prevents hammering a single host",
    "4.  RabbitMQ work queue — consume-and-discard; no replay needed (unlike Kafka)",
    "5.  URL Normaliser — strips utm_* tracking params + fragments to collapse duplicate URLs",
    "6.  MAX_DEPTH=3 — prevents crawler traps (calendars, session IDs generating infinite URLs)",
]
for i,n in enumerate(notes):
    draw.text((NX, 642+i*22), n, fill=rgb(MUTED), font=font(12))

img.save("/home/brennoz77/LABS/LAB_CLAUDE/SystemDesignLab/06-web-crawler/architecture.jpg", "JPEG", quality=95)
print("Saved 06-web-crawler/architecture.jpg")
