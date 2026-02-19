# 카드라 프론트 연동 가이드 (Gemini 브리핑용)

## 1) 기본 색상 토큰
- `--color-main: #00A676`
- `--color-sub: #E0D0C1`
- 권장 보조 색상:
  - `--text-primary: #0F172A`
  - `--text-secondary: #334155`
  - `--bg: #FFFFFF`

## 2) 백엔드 공개 UI API
- `GET /api/v1/ui/theme`
  - 응답: `{ mainColor, subColor, background, textPrimary, textSecondary }`
- `GET /api/v1/ui/contracts`
  - 응답: `theme + route list`

## 3) 핵심 비즈니스 API
- 카드
  - `POST /api/v1/cards/generate`
  - `GET /api/v1/cards/{id}`
- 연구
  - `POST /api/v1/research/run`
  - `POST /api/v1/research/jobs`
  - `GET /api/v1/research/jobs/{jobId}`
  - `GET /api/v1/research/jobs/{jobId}/result`
  - `POST /api/v1/research/jobs/{jobId}/cancel`
- 추천
  - `POST /api/v1/recommend/keywords`
  - `POST /api/v1/recommend/events`

## 4) CardItem 렌더 매핑 (프론트)
- `variant`: `headline | insight | summary` (없으면 default)
- `style.tone/layout/emphasis`: 클래스 매핑 권장
- `media`: 카드 커버
- `cta`: CTA 버튼 노출
- `tags`: 하단 라벨 뱃지
- `imageHint`: 이미지 생성용 프롬프트 힌트

## 5) 에러 처리
- `error.code`, `error.retryable`, `error.retryAfter`를 신호로 사용
  - retryable=true + retryAfter 있으면 버튼 표시 + 자동 재시도 카운트
  - traceId는 로그/디버그용 출력

## 6) 권장 화면 최소 구현(1차)
1. 입력 화면: 키워드 입력 + 추천 키워드 표시
2. 결과 화면: 카드 2~3장 렌더
3. 연구 화면: 동기 실행 + 비동기 job 폴링(

    `jobs/{id}`) + 결과 실패면 retry UX
4. 공통 오류 토스트 + 리트라이

## 7) 참고
- 서브에이전트/디자인 담당 결과가 오면, 본 가이드를 component 파일 기준으로 분해해 `src` 구조에 맞게 바로 반영 가능.
