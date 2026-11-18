# ApproPin — 나만의 숏게임 거리표 (안드로이드 앱)

Golfull의 「나만의 숏게임 거리표」를 따로 떼어 만든 단독 안드로이드 앱이에요. 로그인·회원 기능 없이 바로 쓰고, 입력한 내용은 휴대폰 안에만 저장돼요.

- 앱 이름: ApproPin / 패키지: `com.jungcle.appropin`
- 안드로이드 10 이상
- 아이콘: 적응형 아이콘(원형·둥근사각 모두 대응) + 안드로이드 12 이상 시작 화면(딥그린 배경)
- 세팅: 백스윙 7·9·11시 → 팔로우스루 기본 5시·3시·1시(편집에서 1:1·1:2 선택), 스탠스·그립·볼 포지션 1~3단계(1단계일수록 짧게). 거리는 풀스윙 거리 × 백스윙 비율로 계산하고 자동 추천은 클럽·백스윙만, 나머지는 편집에서 직접 선택
- 화면: 하단 탭(거리 입력 / 내 거리표 / 보관함), 내 거리표는 라운드용 보기 ↔ 편집 전환
- 화면: `app/src/main/assets/index.html` (이 파일만 고치면 앱 화면이 바뀌어요)
- 사진 저장: 갤러리 `Pictures/ApproPin` 폴더 (권한 요청 없음)
- 공유·인쇄(PDF): 안드로이드 공유 창 / 인쇄 창으로 연결
- 후원하기: 토스 앱으로 바로 연결, 토스가 없으면 계좌번호 복사

## 웹에서 쓰기 (GitHub Pages, 무료)

- 주소: **https://woozi3747-ctrl.github.io/Appropin/**
- 배포 위치: `docs/` 폴더 (저장소 Settings → Pages → Source: `Deploy from a branch`, Branch: `main` / `/docs`)
- 앱 화면(`app/src/main/assets/index.html`)을 고쳤을 때는 아래를 한 번 실행해 웹 쪽 사본도 맞춰 주세요.

```bash
python3 scripts/sync_web.py          # docs/index.html 을 앱 화면과 같은 내용으로 맞춤
python3 scripts/make_web_assets.py   # favicon·아이콘·og 이미지·manifest 다시 만들기 (그림을 바꿀 때만)
```

앱 안에서 만든 공유 링크·QR 코드는 `index.html` 의 `SITE_URL` (`woozi3747-ctrl.github.io/Appropin`)로 만들어져서 이 웹 주소로 열려요.

## APK 만드는 법

**A. GitHub Actions (권장)** — `main` 에 푸시하면 자동으로 빌드돼요.
Actions 탭 → 최근 실행 → Artifacts 의 `ApproPin-apk`, 또는 Releases 의 최신 빌드에서 `ApproPin.apk` 를 받으세요.
빌드에 필요한 서명 키는 **저장소 Secrets** 에서 꺼내 써요. 한 번만 아래 4개를 넣어 두면 돼요.
(Settings → Secrets and variables → Actions → New repository secret)

| Secret 이름 | 값 |
| --- | --- |
| `KEYSTORE_B64` | 서명 키 파일을 base64 로 바꾼 문자열 (`base64 -w0 app/release.jks`) |
| `KEYSTORE_PASSWORD` | 키 저장소 비밀번호 |

`KEY_ALIAS`(기본값 `shortgame`)와 `KEY_PASSWORD`(기본값 = 키 저장소 비밀번호)는 바꿀 때만 넣으면 돼요.

**B. Android Studio** — 이 폴더를 열고, 아래 Terminal 탭에서 `./gradlew assembleRelease` (윈도우는 `gradlew assembleRelease`)
→ `app/build/outputs/apk/release/ApproPin-1.2.apk`
로컬 빌드는 프로젝트 맨 위의 `keystore.properties` 를 읽어요. 이 파일과 키 파일(`app/release.jks`)은 **커밋하지 마세요**. (`.gitignore` 에 들어 있어요)

## ⚠️ 서명 키 보관

- 서명 키는 저장소에 올리지 않아요. GitHub Actions Secrets 와, 본인만 쓰는 안전한 곳(비공개 저장소·암호화된 백업)에 보관하세요.
- 키를 잃어버리면 같은 앱으로 업데이트를 올릴 수 없어요. 새 키로 서명하면 다른 앱으로 취급돼서, 쓰던 사람은 기존 앱을 지우고 새로 설치해야 해요.
- v1.2 부터는 서명 키가 새로 바뀌었어요. v1.1 이하를 쓰던 휴대폰은 **기존 앱을 삭제한 뒤 v1.2 를 설치**해야 해요. (지우기 전에 내 거리표를 링크·사진으로 백업하세요)
- 업데이트할 때는 `app/build.gradle` 의 `versionCode` 를 1씩 올려 주세요.
