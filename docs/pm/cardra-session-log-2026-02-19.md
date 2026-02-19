# Cardra Session Log (2026-02-19)

## 목적
- 세션 종료 전, 오늘 진행한 작업과 검증 결과를 한 번에 추적 가능한 형태로 기록.
- 다음 세션에서 바로 이어서 실행할 TODO를 명확히 남김.

## 오늘 완료한 핵심 작업
1. 서버/웹 기본 구조 고도화 및 API 계약 정비
- `cards`, `research`, `recommend`, `images`, `ui`, `health` 공개 API 경로 정리.
- Swagger/OpenAPI 접근 경로(`swagger-ui`, `v3/api-docs`) 활성화.

2. ResearchService 외부 어댑터 기반 전환 및 OpenAI 연동
- OpenAI 기반 research 경로 활성화.
- fallback 정책을 설정값으로 제어하도록 변경:
  - `CARDRA_RESEARCH_ALLOW_STUB_FALLBACK=true|false`
- 요청/성공/실패 로그 추가:
  - `research_openai_request`, `research_openai_success`, `research_openai_http_error`

3. 이미지 생성 AI(OpenAI/Gemini) 연동
- 이미지 생성 API 및 provider status API 구현/정리.
- fallback 정책을 설정값으로 제어:
  - `CARDRA_IMAGE_ALLOW_STUB_FALLBACK=true|false`
- 요청/성공/실패 로그 추가:
  - `image_openai_request`, `image_openai_success`, `image_openai_http_error`
  - `image_fallback_disabled`, `image_fallback_used`

4. 로컬 실호출 검증 환경 정리
- `application-local.yml`(gitignored) 기준으로 실호출 실행.
- 로그 파일 출력 경로 사용:
  - `apps/server/logs/cardra-local.log`

5. 딥리서치 토글 기능 변별력 구현 (Quick vs Deep)
- 카드 생성 요청에 `mode` 추가:
  - `quick` (기본): 빠른 카드 생성 경로
  - `deep`: 리서치 기반 근거/팩트체크/리스크 반영 카드 생성
- 프론트 Create 페이지에서 토글 값을 `mode`로 전달하도록 변경.
- 기존 "카드 생성 + 리서치 잡 병렬 호출" 흐름을 단일 카드 생성 API 모드 분기로 단순화.

## 검증 결과 (오늘 세션)
- 서버 품질 게이트:
  - `cd apps/server && ./gradlew test ktlintCheck build` 통과
- 웹 빌드:
  - `cd apps/web && npm run build` 통과
- 로컬 실호출:
  - `POST /api/v1/research/run` → OpenAI 응답 확인
  - `POST /api/v1/images/generate` → OpenAI 응답 확인 (`usedFallback=false` 케이스 검증)
  - `POST /api/v1/cards/generate` + `mode=quick|deep` → 제목/출처 차이 확인
- 로그 확인:
  - `apps/server/logs/cardra-local.log`에 request/success/error/fallback 로그 기록 확인

## 확인된 동작 요약
- 딥리서치 토글 OFF (`mode=quick`):
  - 빠른 카드 생성 경로 사용.
- 딥리서치 토글 ON (`mode=deep`):
  - 리서치 기반 카드 생성 경로 사용(근거 URL/팩트체크 성격 반영).

## 현재 남은 이슈 / 리스크
1. 카드 Quick 경로의 실AI 전환
- 현재 quick 경로는 외부 카드 에이전트 설정(`cardra.agent.external`)에 따라 fallback(mock)로 동작할 수 있음.
- 완전한 실AI 운영을 위해서는 external agent endpoint 연동이 필요.

2. 시크릿 관리
- 로컬 테스트 키는 반드시 `application-local.yml`(gitignored) 또는 환경변수로만 사용.
- 키가 외부 채널/히스토리에 노출된 경우 즉시 rotate 권장.

3. 딥리서치 결과와 카드 상세 페이지 UX 연결
- 현재는 카드 상세(`/cards/:id`) 중심 흐름.
- 필요 시 `mode=deep` 카드 상세에 근거/리스크 UI를 더 노출하는 개선 여지 존재.

## 다음 세션 시작 TODO (우선순위)
1. Quick 경로 외부 Agent 실제 endpoint 연결 (`cardra.agent.external.enabled=true`)
2. 카드 생성 경로 로그 강화 (`mode`, provider/fallback, latency)
3. deep 카드의 근거 링크 렌더링 UX 보강 (웹 카드 상세 페이지)
4. 운영용 시크릿 주입 정책 확정 (env/secret manager)

## 참고 커밋 (2026-02-19)
- `75c7344` feat: image generation API/provider status + server/web 통합 반영
- `6b302c1` feat: OpenAI integration config 보강
- `dde01b2` docs: 외부 소스 감사 문서 보강
