# Cardra PM 운영/개발 문서 모음 (v2)

## 목적
이 문서는 PM이 코드 개발팀에게 바로 전달 가능한 형태로, 카드라 프로젝트의 **서버 파이프라인**, **에이전트 연동**, **검증 기준**을 정리한다.

## 현재 진행 상황(요약)
- `MockAgentAdapter` + `FallbackAgentAdapter` 연동 완료
- `ExternalAgentAdapter` 실제 호출 뼈대(설정 기반 endpoint/timeout) 추가
- 카드 예외 처리: `CardNotFoundException`, `GlobalExceptionHandler` 반영
- 리서치 API 동기/비동기 계약 구현 완료
  - `POST /api/v1/research/run`
  - `POST /api/v1/research/jobs`
  - `GET /api/v1/research/jobs/{jobId}`
  - `GET /api/v1/research/jobs/{jobId}/result`
  - `POST /api/v1/research/jobs/{jobId}/cancel`
- 리서치 폴백·상태 스펙 정비: `error.code/retryable/retryAfter/traceId/usage/cache`가 `ResearchRunResponse`/`ResearchJobErrorResponse`에 반영
- 추천 MVP API 추가: `POST /api/v1/recommend/keywords`, `POST /api/v1/recommend/events`
  - validation 강화(`userId`/`events` 필수), 폴백 메타(`fallbackUsed`, `fallbackReason`, `strategy`) 제공
- **카드 응답 렌더 메타 반영 시작**
  - `CardItem`에 `variant/style/media/cta/tags/imageHint` 필드 추가
  - mock 생성 카드에 variant/style/media/cta 샘플 채움
- **프론트 연동 편의 API 추가**
  - `GET /api/v1/ui/theme` : 메인/sub 컬러 키 전파
  - `GET /api/v1/ui/contracts` : 프론트에서 바로 쓰는 API 목록 제공
- 테스트: `test` + `ktlintCheck` 유지 통과 (`./gradlew test`, `./gradlew ktlintCheck`)

## 바로 보이는 현재 구조
- Controller: `apps/server/src/main/kotlin/com/cardra/server/api/{Card,Health,Research,Recommend,Ui}Controller.kt`
- Service: `apps/server/src/main/kotlin/com/cardra/server/service/{Card,agent,research,recommend}`
- DTO: `apps/server/src/main/kotlin/com/cardra/server/dto/{Research,Recommend,Ui,Card}Dtos.kt`
- 예외 핸들러: `apps/server/src/main/kotlin/com/cardra/server/exception/GlobalExceptionHandler.kt`
- 테스트: `apps/server/src/test/...`

## 단계별 진행 체크 (계획 대비)
- [완료] 리서치 contract 확정 + 최소 비동기 Job API
- [완료] 폴백/실패 시그니처 정합화 (에러 코드, retry 정책 메타)
- [완료] recommend MVP 엔드포인트 및 유효성/단위 테스트 정리
- [완료] 카드 응답 렌더 메타 기본 스키마 반영 (`variant/style/media/cta/tags/imageHint`)
- [완료] 프론트 연동 인터페이스 제공 API 추가 (`/api/v1/ui/theme`, `/api/v1/ui/contracts`)
- [진행 필요] 리서치 Job 영속화 (현재 in-memory): 다중 인스턴스/재시작 시 상태 손실
- [진행 필요] 리서치/팩트체크 adapter를 외부 데이터 소스로 전환
- [진행 필요] 추천을 in-memory에서 사용자 이벤트 스토어·랭킹/임베딩 구조로 승격
- [진행 필요] 이미지 생성 실제 연동(placeholder/메타 기반 -> 생성 모델 연동)

## PM 핵심 체크포인트
1. 외부 연동은 한 번에 확정하지 말고 `Agent`/`Service` 계층을 나눠 단계 배치
2. 실패를 성공처럼 감추지 말고 `status/fallback`을 명시적으로 추적
3. 카드 생성 품질은 `입력 키워드-카드 구조-출처 신뢰도` 3요소로 품질 게이트 유지

## 다음 7일 액션(코드 우선)
1. `ResearchJobService` 영속/TTL 스토어(최소 Redis/DB 캐시) 전환 + 상태 조회 이력 보존
2. `ResearchService`에서 실 데이터 adapter(뉴스/트렌드/팩트체크) 연결
3. 추천 스토어를 `user_event_history` + 후보 생성기 + fallback 엔진으로 2단계 분리
4. 에러 스키마/리트라이 표준을 Health/API 응답 규약 문서에 묶음
5. 카드 렌더 메타와 실제 디자인 규칙 매핑(variant/style/media/cta/tags) 정합성 점검
6. 프론트용 스타일 토큰/컴포넌트 템플릿 정비 및 이미지 생성 adapter PoC

## 사용 지침
- PM 요청은 항상 **API 계약 변경 + 영향 범위 + 테스트 수정** 형태로 전달
- 코드 리뷰 우선순위: `회귀 없이 계약 안정성` > `기능 확장`

## 13:00 기준 오전 진행 리포트(당일)
### 완료 항목
- `Research` 동기/비동기 엔드포인트 구현 완료 및 예외 처리 강화
- `ResearchJobService`에서 idempotency, 캐시 힌트, 상태/결과/취소 흐름 정합화
- recommend API 유효성/폴백 메타/예측 가능한 응답 스키마 적용
- 카드 응답 DTO에 렌더 메타 필드 초기 반영
- `Front-end 연동 인터페이스 API`(`/api/v1/ui/theme`, `/api/v1/ui/contracts`) 추가
- 모든 변경 테스트 및 스타일 게이트 통과 (`./gradlew test`, `./gradlew ktlintCheck`)

### 진행 중인 오차/갭
- 연구 Job 상태의 영속성이 없어 재시작/스케일아웃 대응 미완
- 추천은 현재 in-memory 이벤트 기반 규칙 로직(실서비스 품질엔 추가 작업 필요)
- 이미지 생성은 템플릿/메타 위주, 실제 생성 모델 연동 미완

### 다음 할 일(오후 바로 착수)
1. 연구 Job 영속/TTL 설계(최소 DB/Redis 캐시) + 상태 조회 보존
2. 추천 후보 생성 분리(이벤트 스토어, 랭킹 엔진, 폴백 사전 정의)
3. 디자이너 규칙-백엔드 렌더 스펙 동기화(variant/style/media/cta) 점검
