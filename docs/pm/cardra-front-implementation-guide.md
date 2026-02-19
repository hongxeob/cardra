# 카드라 프론트엔드 MVP 실행 가이드

## 상태
- `/api/v1` 연동 기준 웹 프론트 MVP를 위해 `apps/web`에 React/Vite 최소 스캐폴딩을 생성함.
- 현재 목표: 프론트 실제 화면 연결을 바로 시작할 수 있는 최소 구조 제공

## 포함 내용
- 라우팅
  - `/` 홈
  - `/create` 생성 화면
  - `/cards/:id` 카드뷰
  - `/research/loading/:jobId` 리서치 진행
  - `/research/:jobId/result` 리서치 결과
  - `/error`, `*` 404

- 상태관리
  - React Query를 이용한 API 상태(loading/success/error) 처리
  - 실패 시 `error.ts`로 표준 AppError 변환

- 렌더 핵심
  - `CardItemBlock`에서 `variant/style/media/cta/tags/imageHint` 반영 초안

- 디자인 토큰
  - `src/styles/tokens.css`에 CSS 변수를 지정하고, `theme.ts`에서 루트 변수 주입

## 실행 방법(현재 단계)
- Node 설치 후
  - `cd apps/web`
  - `npm install`
  - `npm run dev`

## 다음 작업(1차)
- 추천 API UI(추천칩 + 최근 이벤트 전송) 연결
- 리서치 실패 시 retryable 처리(재시도 버튼) 분기 강화
- 결과 타입 정밀화(현재 result 표시용 `unknown` 사용)
