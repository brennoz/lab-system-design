from PIL import Image, ImageDraw, ImageFont
import math

W, H = 1500, 780
BG        = "#0f172a"
BLUE      = "#1e40af"; BLUE_LT   = "#3b82f6"
GREEN     = "#065f46"; GREEN_LT  = "#4ade80"
RED       = "#7f1d1d"; RED_LT    = "#f87171"
PURPLE    = "#4c1d95"; PURPLE_LT = "#a78bfa"
ORANGE    = "#78350f"; ORANGE_LT = "#fb923c"
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

def arrow(x1,y1,x2,y2,c=ARROW):
    draw.line([x1,y1,x2,y2], fill=rgb(c), width=2)
    a = math.atan2(y2-y1, x2-x1); s=10
    draw.polygon([(x2,y2),(x2-s*math.cos(a-0.4),y2-s*math.sin(a-0.4)),(x2-s*math.cos(a+0.4),y2-s*math.sin(a+0.4))],fill=rgb(c))

# Title
draw.text((W//2,34), "01 — Rate Limiter Architecture", fill=rgb(TEXT), font=font(22,True), anchor="mm")
draw.text((W//2,58), "Token Bucket · Sliding Window · Redis atomics · Per-client counters", fill=rgb(MUTED), font=font(13), anchor="mm")

# ── Boxes ─────────────────────────────────────────────────────────────────────
# Client
box(40, 180, 140, 60, SLATE, BLUE_LT, "Client", "HTTP requests", BLUE_LT)

# Rate Limiter Service
box(260, 100, 240, 220, BLUE, BLUE_LT, "Rate Limiter Service", ":8080  Spring Boot", TEXT)
draw.text((380, 175), "POST /api/v1/check",      fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((380, 195), "Token Bucket algorithm",  fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((380, 215), "Sliding Window algorithm", fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((380, 235), "RateLimiterUseCase",       fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((380, 255), "SlidingWindowUseCase",     fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((380, 275), "CheckLimitUseCase",        fill=rgb(MUTED), font=font(11), anchor="mm")

# Redis
box(620, 140, 200, 130, ORANGE, ORANGE_LT, "Redis :6379", None, ORANGE_LT)
draw.text((720, 215), "INCR  token_bucket:{key}", fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((720, 233), "EXPIRE bucket TTL",         fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((720, 251), "ZADD  sliding:{key} ts",   fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((720, 269), "ZREMRANGEBYSCORE ...",      fill=rgb(MUTED), font=font(11), anchor="mm")

# Token Bucket detail
box(920, 110, 200, 80, GREEN, GREEN_LT, "Token Bucket", "O(1) per request", GREEN_LT)
draw.text((1020, 185), "tokens -= 1 per request", fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((1020, 200), "refill at fixed rate",    fill=rgb(MUTED), font=font(11), anchor="mm")

# Sliding Window detail
box(920, 220, 200, 80, RED, RED_LT, "Sliding Window", "O(log N) per request", RED_LT)
draw.text((1020, 295), "ZADD ts → ZCOUNT window", fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((1020, 310), "evict outside window",    fill=rgb(MUTED), font=font(11), anchor="mm")

# Responses
box(620, 330, 200, 60, GREEN, GREEN_LT, "200 OK",      "request allowed",  GREEN_LT)
box(920, 330, 200, 60, RED,   RED_LT,   "429 Too Many", "Retry-After header", RED_LT)

# Prometheus
box(260, 380, 240, 60, PURPLE, PURPLE_LT, "Prometheus :9090", "metrics scrape /actuator", PURPLE_LT)

# Grafana
box(260, 470, 240, 60, PURPLE, PURPLE_LT, "Grafana :3000", "dashboards + alerts", PURPLE_LT)

# ── Arrows ─────────────────────────────────────────────────────────────────────
arrow(180, 210, 260, 210)                         # Client → Service
arrow(500, 200, 620, 200)                         # Service → Redis
arrow(820, 175, 920, 150)                         # Redis → Token Bucket
arrow(820, 230, 920, 260)                         # Redis → Sliding Window
arrow(1020, 190, 1020, 330)                       # Token Bucket → 200
arrow(1020, 300, 1020, 330)                       # Sliding Window → 429
arrow(380, 320, 380, 380)                         # Service → Prometheus
arrow(380, 440, 380, 470)                         # Prometheus → Grafana

# label on arrows
draw.text((540, 170), "INCR / ZADD", fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((870, 155), "bucket check", fill=rgb(MUTED), font=font(11), anchor="mm")
draw.text((870, 245), "window check", fill=rgb(MUTED), font=font(11), anchor="mm")

# ── Legend ─────────────────────────────────────────────────────────────────────
LX, LY = 50, 440
draw.text((LX, LY), "Legend", fill=rgb(TEXT), font=font(13,True))
items = [(BLUE,BLUE_LT,"Spring Boot Service"),(GREEN,GREEN_LT,"Allowed path (200)"),(RED,RED_LT,"Throttled path (429)"),(ORANGE,ORANGE_LT,"Redis (counters + windows)"),(PURPLE,PURPLE_LT,"Observability (Prometheus+Grafana)")]
for i,(f,b,t) in enumerate(items):
    draw.rounded_rectangle([LX,LY+20+i*26,LX+18,LY+38+i*26], radius=3, fill=rgb(f), outline=rgb(b), width=1)
    draw.text((LX+26, LY+29+i*26), t, fill=rgb(MUTED), font=font(12), anchor="lm")

# ── Key notes ─────────────────────────────────────────────────────────────────
NX = 50
draw.text((NX, 590), "Key Design Points", fill=rgb(TEXT), font=font(13,True))
notes = [
    "1.  Token Bucket — fixed capacity, refills at constant rate; allows short bursts",
    "2.  Sliding Window — tracks exact timestamps in sorted set; no burst allowance",
    "3.  Redis atomics (INCR + EXPIRE) — race-free counter updates without Lua scripts",
    "4.  Per-client keys — rate limited independently by API key or IP address",
    "5.  429 response includes Retry-After header so clients back off gracefully",
    "6.  Prometheus metrics exported via Spring Actuator; Grafana shows live dashboards",
]
for i,n in enumerate(notes):
    draw.text((NX, 612+i*22), n, fill=rgb(MUTED), font=font(12))

img.save("/home/brennoz77/LABS/LAB_CLAUDE/SystemDesignLab/01-rate-limiter/architecture.jpg", "JPEG", quality=95)
print("Saved 01-rate-limiter/architecture.jpg")
