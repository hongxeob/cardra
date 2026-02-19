# Cardra 백엔드 Functional Backlog (Codex)

본 문서는 카드라 백엔드(`apps/server`)의 기능 구현 항목을 정리한 우선순위 목록이다.

## 1. 핵심 API 기능

### 1.1 카드 생성 API 고도화
- `POST /api/v1/cards/generate`
  - 입력: `keyword`, `tone`, `locale`, `refresh` 등 확장
  - 출력: 카드 2~3장, 각 카드별 `source`, `sourceAt`, `sourceLinks`
  - 실패 시 안전 응답(폴백 메시지) 적용

### 1.2 카드 조회 API 보강
- `GET /api/v1/cards/{id}`
  - 404 처리 구조화 (에러 코드/메시지 통일)
  - 조회 캐시/재생성 플래그(`isStale`) 추가 검토

### 1.3 헬스/준비 상태 API
- `GET /api/v1/health`
  - DB 연결 상태 포함 가능하도록 확장 (Readiness 체크)

## 2. 데이터/보존 계층

### 2.1 카드 저장소
- Card 엔티티 메타데이터 확장
  - `source_count`, `source_links_json`, `agent_version`, `generated_by`, `requested_locale`
- 조회 성능을 위한 인덱스 추가

### 2.2 이벤트/이력
- 요청 이력 테이블(`card_requests`) 도입(옵션)
  - 요청 시각, 키워드, 소요시간, 정책(타임아웃/폴백 유무)

## 3. 에이전트 연동 레이어

### 3.1 Adapter 분리
- 현재 `MockAgentAdapter`를 실제 외부 호출 가능한 구조로 확장
- 인터페이스 정리:
  - `AgentAdapter` (현재): 생성 기능
  - `ResearchProvider` (현재): 소스 후보 수집
- 신규:
  - `ExternalAgentAdapter` (Codex가 담당): 실제 리서치/요약 호출
  - `SafeAdapterDecorator` (선택): 실패/재시도 정책 통합

### 3.2 실패 정책
- timeout/retry/backoff 정책 값화 (`application.yml`)
- 실패 시 `FallbackAgentAdapter` 자동 fallback

## 4. 검증/품질

### 4.1 테스트 보강
- Service/Controller 테스트
  - 성공/실패 분기(400, 404, 500) 모두 추가
- 통합 테스트
  - `@SpringBootTest` + Testcontainers(PostgreSQL) 구성 검토

### 4.2 스타일/CI 안정성
- `ktlintCheck`를 커밋/푸시 전 강제
- pre-commit/pre-push 훅 경로 고정 상태 유지

## 5. 운영/배포

### 5.1 환경 설정 분리
- `application.yml` 프로필 분리 (`local`, `docker`, `prod`)
- 외부 API 키/endpoint는 환경변수 주입

### 5.2 배포 파이프라인
- Docker image 빌드 안정성 개선
- DB 마이그레이션 전략(Flyway/liquibase) 도입 검토

## 6. 추적 우선순위 (P0/P1/P2)

- **P0**: 실패 대응 안정성 (`FallbackAgentAdapter`, 에러 응답 일관성)
- **P0**: `GET /cards/{id}` 404 표준화
- **P1**: ExternalAgentAdapter 기본 연동
- **P1**: 카드 결과 메타데이터 확장(source metadata)
- **P2**: 요청 이력 저장, observability 추가

---

작성일: 2026-02-19
최종 수정자: Codex
