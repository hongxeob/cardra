# Cardra 백엔드 서버 로드맵 (Superpowers Edition)

## 목표
Spring Boot(Kotlin) 서버를 "로컬 MVP"에서 "운영 가능한 파이프라인"으로 단계적으로 이동.

## 단계별 실행

### 1) 핵심 품질 게이트(즉시)
- **Task 1.1**: AGENT 어댑터 인터페이스 안정화
  - 현재 `service.agent` 인터페이스/구현을 유지하되, 실제 호출 실패/타임아웃 정책 주석 추가
- **Task 1.2**: 테스트 커버리지 확장
  - `CardService` 단위 테스트 보강
  - `CardController` Web API 테스트 추가
- **Task 1.3**: 코드 형식 강제
  - `ktlintCheck` + `test` 를 CI(또는 pre-push)에서 반드시 통과

### 2) 외부 연동 추상화(다음)
- **Task 2.1**: 에이전트 구현체 분리
  - `MockAgentAdapter`와 별도 `ExternalAgentAdapter` 인터페이스 분리
- **Task 2.2**: provider 별 토큰/타임아웃/리트라이 설정
  - 5xx/네트워크 실패 시 fallback으로 mock-safe 템플릿 반환
- **Task 2.3**: 출처/타임스탬프 표준 스키마 확정
  - 카드 item에 `sourceAt`, `sources[]` 형식 통일

### 3) 계약 안정화
- **Task 3.1**: OpenAPI(또는 API 문서) 초안
- **Task 3.2**: 에러 코드 규격 정리
  - `400`, `404`, `500` 공통 형식 응답 확정

### 4) 운영/배포
- **Task 4.1**: `docker compose` 실행 검증 (DB+server)
- **Task 4.2**: 빌드/테스트 실패 시 알림 가이드 정리

## Superpower 역할 분담(현재 적용)
- **Codex (PM/총괄)**: 단계 관리, API 계약/에러 정책 확정
- **Codex (구현)**: 서비스·테스트·CI 파이프라인 작성
- **Gemini (리서치/분석)**: 요구사항 보강/리스크 포인트 제안(문서)

## 실행 우선순위(오늘)
1. CI에 `test + ktlintCheck` 연결
2. API 테스트 추가
3. AgentAdapter 예외 처리/타임아웃 정책 문서화
4. 문서 업데이트 후 팀 공유

## 성공 기준
- `./gradlew test`/`./gradlew ktlintCheck` 통과
- `/api/v1/cards/generate` 정상 응답 2~3장 반환
- 주요 브랜치 푸시 전 자동 검증 체계 작동
