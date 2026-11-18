#!/usr/bin/env python3
"""앱 화면(app/src/main/assets/index.html)을 웹 배포본(docs/index.html)으로 복사해요.

    python3 scripts/sync_web.py

GitHub Pages 는 docs/ 폴더를 그대로 올리기 때문에, 화면을 고친 뒤 이 스크립트를 한 번 돌려
두 사본을 같게 맞춰 주세요.
"""
import os
import shutil
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "app", "src", "main", "assets", "index.html")
DST = os.path.join(ROOT, "docs", "index.html")

NEEDS = ["favicon.png", "apple-touch-icon.png", "manifest.webmanifest", "og.png"]


def main():
    if not os.path.exists(SRC):
        sys.exit(f"앱 화면을 찾을 수 없어요: {SRC}")
    os.makedirs(os.path.dirname(DST), exist_ok=True)
    shutil.copyfile(SRC, DST)

    missing = [n for n in NEEDS if not os.path.exists(os.path.join(ROOT, "docs", n))]
    if missing:
        print("⚠️ docs/ 에 없는 파일:", ", ".join(missing), "→ python3 scripts/make_web_assets.py 를 실행하세요")

    if "appropin.netlify.app" in open(DST, encoding="utf-8").read():
        print("⚠️ docs/index.html 에 아직 appropin.netlify.app 주소가 있어요. SITE_URL 을 확인하세요")

    print(f"복사 완료: {os.path.relpath(SRC, ROOT)} → {os.path.relpath(DST, ROOT)}")


if __name__ == "__main__":
    main()
