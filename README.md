# ApproPin — 나만의 숏게임 거리표 (안드로이드 앱)

Golfull의 「나만의 숏게임 거리표」를 따로 떼어 만든 단독 안드로이드 앱이에요. 로그인·회원 기능 없이 바로 쓰고, 입력한 내용은 휴대폰 안에만 저장돼요.

- 앱 이름: ApproPin / 패키지: `com.jungcle.appropin`
- 안드로이드 10 이상
- 아이콘: 적응형 아이콘(원형·둥근사각 모두 대응) + 안드로이드 12 이상 시작 화면(딥그린 배경)
- 세팅: 백스윙 7·9·11시 → 팔로우스루 기본 4시·3시·1시(편집에서 1:1·1:2 선택), 스탠스·그립·볼 포지션 1~3단계(1단계일수록 짧게). 거리는 풀스윙 거리 × 백스윙 비율로 계산하고 자동 추천은 클럽·백스윙만, 나머지는 편집에서 직접 선택
- 화면: 하단 탭(거리 입력 / 내 거리표 / 보관함), 내 거리표는 라운드용 보기 ↔ 편집 전환
- 화면: `app/src/main/assets/index.html` (이 파일만 고치면 앱 화면이 바뀌어요)
- 사진 저장: 갤러리 `Pictures/ApproPin` 폴더 (권한 요청 없음)
- 공유·인쇄(PDF): 안드로이드 공유 창 / 인쇄 창으로 연결
- 후원하기: 토스 앱으로 바로 연결, 토스가 없으면 계좌번호 복사

## APK 만드는 법

**A. Android Studio** — 이 폴더를 열고, 아래 Terminal 탭에서 `./gradlew assembleRelease` (윈도우는 `gradlew assembleRelease`)
→ `app/build/outputs/apk/release/ApproPin-1.0.apk`

**B. GitHub Actions** — 이 폴더를 GitHub 저장소(비공개 권장)에 올리면 자동으로 빌드돼요.
Actions 탭 → 최근 실행 → Artifacts 의 `ApproPin-apk` 다운로드.

## ⚠️ 서명 키 보관

`app/release.jks` 와 `keystore.properties` 는 앱 서명 키예요. 업데이트 버전을 같은 키로 서명해야 기존 앱 위에 덮어 설치돼요.
잃어버리지 않게 따로 백업하고, 공개 저장소에는 올리지 마세요.

업데이트할 때는 `app/build.gradle` 의 `versionCode` 를 1씩 올려 주세요.
