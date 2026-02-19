# 카드라 프론트엔드 MVP 실행 가이드

## 상태
- `/api/v1` 연동 기준 웹 프론트 MVP를 위해 `apps/web`에 React/Vite 기반 스캐폴딩 생성 완료.
- 1차 연동 항목(라우팅/카드 조회/생성/리서치 폴링) 이후 **2차 연동(추천 API + retry UX + 타입 정밀화)**를 진행 중.

## 현재 반영 항목
- 추천 API 연동 준비
  - `/recommend/keywords` 연동 훅/컴포넌트 추가
  - 추천 키워드 칩 컴포넌트 렌더링 및 키워드 클릭 채우기
  - `POST /recommend/events` 이벤트 전송(카드 생성 성공 시)
- 재시도/에러 UX 개선
  - `ErrorCard`, `LoadingCard` 공통 컴포넌트 추가
  - 리서치/카드 페이지에서 retryable 에러에 대한 재시도 버튼 노출
  - 리서치 로딩 페이지에서 상태 조회 실패 시 재시도/결과 화면 이동
- 타입 정밀화
  - `Research*` 응답 타입 분해(아이템/요약/factcheck/에러/사용량/캐시)
  - `recommend` 이벤트 타입까지 DTO 정리

## 포함 라우팅
- `/` 홈
- `/create` 생성 화면
- `/cards/:id` 카드뷰
- `/research/loading/:jobId` 리서치 진행
- `/research/:jobId/result` 리서치 결과
- `/error`, `*`(404)

## 실행 방법
- `cd apps/web`
- `npm install`
- `npm run dev`
- 백엔드 동작 시 `vite.config.ts` 프록시(`/api -> http://localhost:8080`) 사용

## 2차 TODO
- 추천 후보 카드 UX를 더 풍부하게(리스크 태그/확신도 표시)
- 성공/실패 텔레메트리 기반 toast 알림 추가
- 리서치 실패 시 `retryAfter` 카운트다운 기반 버튼 제어
- 결과 화면 카드형으로 `ResearchRunResponse` 가독성 개선
