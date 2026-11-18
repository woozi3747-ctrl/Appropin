#!/usr/bin/env python3
"""웹(GitHub Pages) 배포용 이미지·manifest 를 다시 만들어요.

    python3 scripts/make_web_assets.py

docs/ 안의 favicon.png, apple-touch-icon.png, icon-192.png, icon-512.png, og.png,
manifest.webmanifest 를 새로 씁니다. (앱 아이콘과 같은 색·모양으로 그려요)
"""
import json
import os
from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCS = os.path.join(ROOT, "docs")
KR_FONT = "/usr/share/fonts/pickle-hwp/NotoSansCJKkr-Regular.otf"

BG = (9, 72, 40)          # #094828
MOUND = (131, 207, 77)    # 잔디
FLAG = (166, 225, 79)     # 깃발
POLE = (255, 255, 255)
SHADOW = (74, 150, 60)


def draw_logo(size, radius_ratio=0.22):
    """앱 아이콘과 같은 그림(깃발+볼)을 size×size 로 그려요."""
    ss = 4 if size <= 256 else 2          # 계단 현상 방지용 배율
    n = size * ss
    img = Image.new("RGB", (n, n), BG)
    d = ImageDraw.Draw(img)

    # 잔디 언덕
    d.ellipse([n * 0.10, n * 0.44, n * 0.90, n * 0.72], fill=MOUND)
    # 깃대
    d.rectangle([n * 0.472, n * 0.16, n * 0.492, n * 0.56], fill=POLE)
    # 깃발
    d.polygon([(n * 0.492, n * 0.165), (n * 0.492, n * 0.29), (n * 0.74, n * 0.225)], fill=FLAG)
    # 볼 그림자
    d.ellipse([n * 0.535, n * 0.545, n * 0.665, n * 0.585], fill=SHADOW)
    # 볼
    d.ellipse([n * 0.545, n * 0.495, n * 0.655, n * 0.565], fill=(255, 255, 255))

    img = img.resize((size, size), Image.LANCZOS)
    return img


def rounded(img, ratio=0.22):
    """모서리를 둥글게 (apple-touch-icon·og 용)."""
    w, h = img.size
    r = int(min(w, h) * ratio)
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, w - 1, h - 1], radius=r, fill=255)
    out = Image.new("RGB", (w, h), BG)
    out.paste(img, (0, 0), mask)
    return out


def font(size, strike=0):
    return ImageFont.truetype(KR_FONT, size)


def make_og():
    W, H = 1200, 630
    img = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(img)
    # 왼쪽 위에서 오른쪽 아래로 아주 살짝 밝아지는 결
    top = BG
    bottom = (11, 92, 50)
    for i in range(H):
        t = i / (H - 1)
        c = tuple(int(top[k] + (bottom[k] - top[k]) * t) for k in range(3))
        d.line([(0, i), (W, i)], fill=c)

    icon = rounded(draw_logo(300, 0.16), 0.24)
    img.paste(icon, (96, (H - 300) // 2))

    x = 96 + 300 + 76
    d.text((x, 150), "ApproPin", font=font(104, 1), fill=(255, 255, 255), stroke_width=1, stroke_fill=(255, 255, 255))
    d.text((x, 296), "나만의 숏게임 거리표", font=font(56), fill=FLAG)
    d.text((x, 388), "웨지 풀스윙 거리로 만드는 100m 안쪽 샷", font=font(32), fill=(206, 232, 214))
    d.text((x, 434), "백스윙 · 팔로우스루 · 스탠스 · 그립 · 볼 포지션", font=font(28), fill=(168, 205, 181))
    d.text((x, 508), "Made by KPGA Class A 정우재 프로", font=font(28), fill=(140, 186, 156))
    return img


def main():
    os.makedirs(DOCS, exist_ok=True)

    draw_logo(64).save(os.path.join(DOCS, "favicon.png"))
    rounded(draw_logo(180, 0.16), 0.22).save(os.path.join(DOCS, "apple-touch-icon.png"))
    draw_logo(192).save(os.path.join(DOCS, "icon-192.png"))
    draw_logo(512).save(os.path.join(DOCS, "icon-512.png"))
    make_og().save(os.path.join(DOCS, "og.png"))

    manifest = {
        "name": "ApproPin — 나만의 숏게임 거리표",
        "short_name": "ApproPin",
        "lang": "ko",
        "start_url": "./",
        "scope": "./",
        "display": "standalone",
        "orientation": "portrait",
        "background_color": "#094828",
        "theme_color": "#094828",
        "icons": [
            {"src": "icon-192.png", "sizes": "192x192", "type": "image/png", "purpose": "any"},
            {"src": "icon-512.png", "sizes": "512x512", "type": "image/png", "purpose": "any"},
            {"src": "icon-512.png", "sizes": "512x512", "type": "image/png", "purpose": "maskable"},
        ],
    }
    with open(os.path.join(DOCS, "manifest.webmanifest"), "w", encoding="utf-8") as f:
        json.dump(manifest, f, ensure_ascii=False, indent=2)
        f.write("\n")

    for name in ["favicon.png", "apple-touch-icon.png", "icon-192.png", "icon-512.png", "og.png", "manifest.webmanifest"]:
        p = os.path.join(DOCS, name)
        print(f"{name:24s} {os.path.getsize(p):>8,d} bytes")


if __name__ == "__main__":
    main()
