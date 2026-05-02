from PIL import Image, ImageDraw, ImageFont
import os

W, H = 1600, 820
BG       = "#0f172a"
BLUE     = "#1e40af"
BLUE_LT  = "#3b82f6"
GREEN    = "#065f46"
GREEN_LT = "#4ade80"
RED      = "#7f1d1d"
RED_LT   = "#f87171"
PURPLE   = "#4c1d95"
PURPLE_LT= "#a78bfa"
ORANGE   = "#78350f"
ORANGE_LT= "#fb923c"
SLATE    = "#1e293b"
TEXT     = "#e2e8f0"
MUTED    = "#94a3b8"
ARROW    = "#475569"

img  = Image.new("RGB", (W, H), BG)
draw = ImageDraw.Draw(img)

def hex2rgb(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def box(x, y, w, h, fill, border, label, sublabel=None, label_color=TEXT):
    draw.rounded_rectangle([x, y, x+w, y+h], radius=10,
                           fill=hex2rgb(fill), outline=hex2rgb(border), width=2)
    try:
        font  = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 15)
        font2 = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 12)
    except:
        font = font2 = ImageFont.load_default()
    cx = x + w // 2
    if sublabel:
        draw.text((cx, y + h//2 - 11), label,    fill=hex2rgb(label_color), font=font,  anchor="mm")
        draw.text((cx, y + h//2 + 10), sublabel, fill=hex2rgb(MUTED),       font=font2, anchor="mm")
    else:
        draw.text((cx, y + h//2), label, fill=hex2rgb(label_color), font=font, anchor="mm")

def arrow(x1, y1, x2, y2, color=ARROW):
    draw.line([x1, y1, x2, y2], fill=hex2rgb(color), width=2)
    # arrowhead
    import math
    angle = math.atan2(y2 - y1, x2 - x1)
    size  = 10
    ax1 = x2 - size * math.cos(angle - 0.4)
    ay1 = y2 - size * math.sin(angle - 0.4)
    ax2 = x2 - size * math.cos(angle + 0.4)
    ay2 = y2 - size * math.sin(angle + 0.4)
    draw.polygon([(x2, y2), (ax1, ay1), (ax2, ay2)], fill=hex2rgb(color))

def label(x, y, text, color=MUTED, size=11):
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", size)
    except:
        font = ImageFont.load_default()
    draw.text((x, y), text, fill=hex2rgb(color), font=font, anchor="mm")

# ── Title ──────────────────────────────────────────────────────────────────────
try:
    title_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 22)
    sub_font   = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 13)
except:
    title_font = sub_font = ImageFont.load_default()

draw.text((W//2, 36), "07 — Notification System Architecture", fill=hex2rgb(TEXT), font=title_font, anchor="mm")
draw.text((W//2, 60), "Fan-out on Write · Two-Lane Priority Queues · Strategy Pattern (Channels) · Idempotency", fill=hex2rgb(MUTED), font=sub_font, anchor="mm")

# ── Column X centres ──────────────────────────────────────────────────────────
#  Client  NotifSvc  RabbitMQ  Workers   Channels   PostgreSQL(below NotifSvc)
CX = [100, 390, 680, 940, 1200]
BW, BH = 160, 64   # default box size

# ── ROW 1: Client ─────────────────────────────────────────────────────────────
box(CX[0]-80, 100, 160, BH, SLATE, BLUE_LT, "Client", "POST /send", BLUE_LT)

# ── ROW 2: Notification Service (tall, centred) ───────────────────────────────
NS_X, NS_Y, NS_W, NS_H = CX[1]-110, 85, 220, 160
box(NS_X, NS_Y, NS_W, NS_H, BLUE, BLUE_LT, "Notification Service", ":8082  Spring Boot", TEXT)
try:
    small = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 11)
except:
    small = ImageFont.load_default()
for i, line in enumerate(["POST /api/v1/notifications/send", "PUT  /api/v1/preferences/{id}", "GET  /api/v1/notifications/inbox/{id}"]):
    draw.text((NS_X + NS_W//2, NS_Y + 90 + i*18), line, fill=hex2rgb(MUTED), font=small, anchor="mm")

# ── ROW 3: RabbitMQ queues ───────────────────────────────────────────────────
RMQ_X = CX[2] - 90
box(RMQ_X, 110, 180, 56, RED, RED_LT,   "notifications.critical", "RabbitMQ  CRITICAL", RED_LT)
box(RMQ_X, 190, 180, 56, GREEN, GREEN_LT,"notifications.bulk",     "RabbitMQ  BULK",     GREEN_LT)

# ── ROW 4: Workers ────────────────────────────────────────────────────────────
WK_X = CX[3] - 80
box(WK_X, 110, 160, 56, RED, RED_LT,    "Critical Worker", "fast consumer",    RED_LT)
box(WK_X, 190, 160, 56, GREEN, GREEN_LT,"Bulk Worker",     "rate-limited",     GREEN_LT)

# ── ROW 5: Channels ───────────────────────────────────────────────────────────
CH_X = CX[4] - 80
box(CH_X,  90, 160, 50, PURPLE, PURPLE_LT,"Email Channel",  "WireMock :8089", PURPLE_LT)
box(CH_X, 155, 160, 50, PURPLE, PURPLE_LT,"SMS Channel",    "WireMock :8089", PURPLE_LT)
box(CH_X, 220, 160, 50, ORANGE, ORANGE_LT,"In-App Channel", "PostgreSQL",      ORANGE_LT)

# ── PostgreSQL (bottom of NotifSvc) ──────────────────────────────────────────
PG_X, PG_Y = NS_X - 10, 310
box(PG_X, PG_Y, NS_W + 20, 64, ORANGE, ORANGE_LT, "PostgreSQL", "notifications · preferences · in_app", ORANGE_LT)

# ── WireMock box (shared) ─────────────────────────────────────────────────────
WM_X, WM_Y = CH_X - 10, 310
box(WM_X, WM_Y, 180, 56, PURPLE, PURPLE_LT, "WireMock :8089", "simulates SendGrid+Twilio", PURPLE_LT)

# ── Arrows ───────────────────────────────────────────────────────────────────
# Client → Notification Service
arrow(CX[0]+80, 132, NS_X, 155)

# NotifSvc → RabbitMQ critical
arrow(NS_X + NS_W, 135, RMQ_X, 138)
# NotifSvc → RabbitMQ bulk
arrow(NS_X + NS_W, 200, RMQ_X, 218)

# RabbitMQ critical → Critical Worker
arrow(RMQ_X + 180, 138, WK_X, 138)
# RabbitMQ bulk → Bulk Worker
arrow(RMQ_X + 180, 218, WK_X, 218)

# Critical Worker → Email
arrow(WK_X + 160, 130, CH_X, 115)
# Bulk Worker → SMS
arrow(WK_X + 160, 218, CH_X, 180)
# Bulk Worker → In-App
arrow(WK_X + 160, 230, CH_X, 245)

# NotifSvc → PostgreSQL (down)
arrow(NS_X + NS_W//2, NS_Y + NS_H, NS_X + NS_W//2, PG_Y)

# Email/SMS → WireMock
arrow(CH_X + 80, 140, WM_X + 90, WM_Y)
arrow(CH_X + 80, 180, WM_X + 90, WM_Y + 56)

# In-App → PostgreSQL (line across bottom)
arrow(CH_X, 245, PG_X + NS_W + 20, PG_Y + 32)

# ── Legend ────────────────────────────────────────────────────────────────────
LX, LY = 50, 440
try:
    lfont = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 13)
    lf2   = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 12)
except:
    lfont = lf2 = ImageFont.load_default()

draw.text((LX, LY), "Legend", fill=hex2rgb(TEXT), font=lfont)
items = [
    (BLUE,   BLUE_LT,   "Spring Boot Service"),
    (RED,    RED_LT,    "CRITICAL priority path"),
    (GREEN,  GREEN_LT,  "BULK priority path"),
    (PURPLE, PURPLE_LT, "External channel (WireMock)"),
    (ORANGE, ORANGE_LT, "PostgreSQL storage"),
]
for i, (fill, border, text) in enumerate(items):
    draw.rounded_rectangle([LX, LY+20+i*26, LX+18, LY+38+i*26], radius=3, fill=hex2rgb(fill), outline=hex2rgb(border), width=1)
    draw.text((LX+26, LY+29+i*26), text, fill=hex2rgb(MUTED), font=lf2, anchor="lm")

# ── Flow notes ────────────────────────────────────────────────────────────────
NX = 50
try:
    nfont = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 12)
    nbold = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 13)
except:
    nfont = nbold = ImageFont.load_default()

draw.text((NX, 590), "Key Flows", fill=hex2rgb(TEXT), font=nbold)
notes = [
    "1.  Client POSTs notification request with recipients + channel + priority",
    "2.  SendNotificationUseCase checks preferences → skips opted-out recipients",
    "3.  FanOutService creates one NotificationTask per active recipient",
    "4.  Tasks routed to CRITICAL or BULK RabbitMQ queue by priority",
    "5.  Worker picks up task → idempotency check (UUID) → dispatch to ChannelPort",
    "6.  Email/SMS delivered via WireMock (simulates SendGrid/Twilio)",
    "7.  In-App stored in PostgreSQL → polled by GET /inbox/{recipientId}",
]
for i, note in enumerate(notes):
    draw.text((NX, 612+i*22), note, fill=hex2rgb(MUTED), font=nfont)

out = "/home/brennoz77/LABS/LAB_CLAUDE/SystemDesignLab/07-notification-system/architecture.jpg"
img.save(out, "JPEG", quality=95)
print(f"Saved: {out}")
