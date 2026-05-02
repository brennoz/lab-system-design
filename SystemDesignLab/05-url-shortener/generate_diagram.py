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

def arrow(x1,y1,x2,y2,c=ARROW,label=None):
    draw.line([x1,y1,x2,y2], fill=rgb(c), width=2)
    a = math.atan2(y2-y1, x2-x1); s=10
    draw.polygon([(x2,y2),(x2-s*math.cos(a-0.4),y2-s*math.sin(a-0.4)),(x2-s*math.cos(a+0.4),y2-s*math.sin(a+0.4))],fill=rgb(c))
    if label:
        mx, my = (x1+x2)//2, (y1+y2)//2
        draw.text((mx, my-10), label, fill=rgb(MUTED), font=font(11), anchor="mm")

# Title
draw.text((W//2,34), "05 — URL Shortener Architecture", fill=rgb(TEXT), font=font(22,True), anchor="mm")
draw.text((W//2,58), "Base62 encoding · CQRS · Cache-Aside · Bloom Filter", fill=rgb(MUTED), font=font(13), anchor="mm")

# ── WRITE PATH (top) ──────────────────────────────────────────────────────────
draw.text((60, 88), "WRITE PATH  (POST /api/v1/shorten)", fill=rgb(GREEN_LT), font=font(13,True))

box(40,  105, 130, 56, SLATE,  BLUE_LT,  "Client",         "POST /shorten",     BLUE_LT)
box(230, 105, 200, 56, BLUE,   BLUE_LT,  "URL Shortener",  ":8080 Spring Boot", TEXT)
box(500, 105, 190, 56, ORANGE, ORANGE_LT,"PostgreSQL",      "INSERT url → id",   ORANGE_LT)
box(760, 105, 190, 56, GREEN,  GREEN_LT, "Base62 Encoder", "id → abc123",        GREEN_LT)
box(1020,105, 190, 56, PURPLE, PURPLE_LT,"Bloom Filter",   "Redis SETBIT",       PURPLE_LT)
box(1270,105, 160, 56, GREEN,  GREEN_LT, "Response",       "shortUrl returned",  GREEN_LT)

arrow(170, 133, 230, 133)
arrow(430, 133, 500, 133)
arrow(690, 133, 760, 133, GREEN_LT, "encode id")
arrow(950, 133, 1020,133, PURPLE_LT,"mark seen")
arrow(1210,133, 1270,133)

# ── READ PATH (middle) ────────────────────────────────────────────────────────
draw.text((60, 198), "READ PATH  (GET /r/{code})", fill=rgb(RED_LT), font=font(13,True))

box(40,  215, 130, 56, SLATE,  BLUE_LT,  "Client",       "GET /r/abc123",     BLUE_LT)
box(230, 215, 190, 56, BLUE,   BLUE_LT,  "URL Shortener","decode Base62 → id",TEXT)
box(480, 215, 180, 56, PURPLE, PURPLE_LT,"Bloom Filter", "mightContain?",      PURPLE_LT)
box(730, 215, 160, 56, RED,    RED_LT,   "404",          "URL never existed",  RED_LT)

# Bloom says yes → cache
box(480, 320, 180, 56, ORANGE, ORANGE_LT,"Redis Cache",   "Cache-Aside lookup", ORANGE_LT)
box(730, 320, 160, 56, GREEN,  GREEN_LT, "302 Redirect",  "cache hit",          GREEN_LT)

# Cache miss → postgres
box(480, 420, 180, 56, ORANGE, ORANGE_LT,"PostgreSQL",    "SELECT by id",       ORANGE_LT)
box(730, 420, 160, 56, GREEN,  GREEN_LT, "302 Redirect",  "cache miss → store", GREEN_LT)

arrow(170, 243, 230, 243)
arrow(420, 243, 480, 243)
arrow(660, 243, 730, 243, RED_LT, "no → 404")
# Bloom yes → cache
arrow(570, 271, 570, 320, GREEN_LT)
draw.text((590, 295), "yes", fill=rgb(GREEN_LT), font=font(11))
arrow(660, 348, 730, 348, GREEN_LT, "hit → redirect")
# cache miss
arrow(570, 376, 570, 420, ARROW)
draw.text((590, 400), "miss", fill=rgb(MUTED), font=font(11))
arrow(660, 448, 730, 448, GREEN_LT)

# write-through on miss
arrow(820, 420, 820, 348, ORANGE_LT)
draw.text((835, 385), "store", fill=rgb(MUTED), font=font(11))

# ── Shared infra column ───────────────────────────────────────────────────────
box(1050, 280, 200, 80, ORANGE, ORANGE_LT, "PostgreSQL", "urls table (BIGSERIAL)", ORANGE_LT)
box(1050, 390, 200, 80, ORANGE, ORANGE_LT, "Redis",      "cache + bloom filter",   ORANGE_LT)

# ── Legend ─────────────────────────────────────────────────────────────────────
LX, LY = 50, 510
draw.text((LX, LY), "Legend", fill=rgb(TEXT), font=font(13,True))
items = [(BLUE,BLUE_LT,"Spring Boot Service"),(GREEN,GREEN_LT,"Success / write path"),(RED,RED_LT,"Error path (404)"),(ORANGE,ORANGE_LT,"Storage (PostgreSQL / Redis)"),(PURPLE,PURPLE_LT,"Bloom Filter (dedup gate)")]
for i,(f,b,t) in enumerate(items):
    draw.rounded_rectangle([LX,LY+20+i*26,LX+18,LY+38+i*26], radius=3, fill=rgb(f), outline=rgb(b), width=1)
    draw.text((LX+26, LY+29+i*26), t, fill=rgb(MUTED), font=font(12), anchor="lm")

# ── Key notes ─────────────────────────────────────────────────────────────────
NX = 50
draw.text((NX, 660), "Key Design Points", fill=rgb(TEXT), font=font(13,True))
notes = [
    "1.  Base62 not Base64 — URL-safe alphabet (no +/=); auto-increment ID avoids collisions",
    "2.  Bloom Filter gates read path — cheap GETBIT check before any DB hit",
    "3.  Cache-Aside — app manages Redis; on miss, fetches PostgreSQL then writes to cache",
    "4.  CQRS — ShortenUrlUseCase (write) and ResolveUrlUseCase (read) are separate",
]
for i,n in enumerate(notes):
    draw.text((NX, 682+i*22), n, fill=rgb(MUTED), font=font(12))

img.save("/home/brennoz77/LABS/LAB_CLAUDE/SystemDesignLab/05-url-shortener/architecture.jpg", "JPEG", quality=95)
print("Saved 05-url-shortener/architecture.jpg")
