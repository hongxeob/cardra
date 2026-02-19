# Cardra Server Follow-up Task Plan (2026-02-19)

## Context
최근 서버 작업에서 Research 영역의 구조를 stub 단일 구현에서 adapter 기반으로 전환했다.

완료된 기반 작업:
- `ResearchService`가 `ResearchDataAdapter`를 주입받아 결과를 구성하도록 변경
- `ExternalResearchDataAdapter` + `FallbackResearchDataAdapter` + `StubResearchDataAdapter` 추가
- 외부 리서치 설정 키 추가: `cardra.research.external.{enabled,timeout-seconds,endpoint}`
- 서비스/어댑터 단위 테스트 추가
- 검증 통과: `./gradlew test`, `./gradlew ktlintCheck build`

현재 목표는 이 기반을 실제 운영 가능한 수준으로 끌어올리고, 카드 생성 파이프라인과 계약 일관성을 강화하는 것이다.

## Work Objectives
1. 리서치 외부 어댑터를 안정적으로 운영 가능한 계약으로 고정
2. API 레벨에서 fallback/실패 시나리오 회귀를 방지
3. 리서치 결과를 카드 생성 흐름에 연결할 준비를 완료

## Guardrails
### Must Have
- 기존 API 응답 스키마(`ResearchRunResponse`) 호환성 유지
- fallback 동작은 실패 시에도 안정 응답을 반환
- 모든 변경은 `apps/server` 범위에서 테스트/린트/빌드 통과

### Must NOT Have
- 프론트엔드 계약 변경(필드 제거/의미 변경)
- 무관한 대규모 리팩터링
- 테스트 우회(삭제/비활성화)

## Task Flow
1. External Research Adapter 계약 고정 및 예외 매핑 보강
2. Research API 통합 테스트(성공/외부 실패/fallback) 추가
3. ResearchService 품질 메타(usage/cache/fallback reason) 검증 강화
4. CardService와 Research 결과 연결용 인터페이스 설계/스켈레톤 반영
5. 최종 검증 및 운영 문서 업데이트

## Detailed TODOs

### Task 1. External Adapter 계약 고정
- 대상 파일:
  - `apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt`
  - `apps/server/src/main/resources/application.yml`
- 작업:
  - 외부 응답 필수 필드 검증 규칙 명시
  - HTTP 상태별 예외 매핑 정책 확정(429/5xx/기타)
  - timeout/endpoint 설정 기본값 및 경계값 점검
- Acceptance Criteria:
  - 잘못된 외부 응답에서 `ExternalResearchSchemaError`가 일관되게 발생
  - 429/5xx/기타 오류가 서로 다른 예외 타입으로 구분됨
  - 설정 누락 시 명확한 에러 메시지 확인 가능

### Task 2. API 통합 회귀 테스트 보강
- 대상 파일:
  - `apps/server/src/test/kotlin/com/cardra/server/api/ResearchControllerTest.kt`
  - `apps/server/src/test/kotlin/com/cardra/server/service/research/ExternalResearchDataAdapterTest.kt`
  - `apps/server/src/test/kotlin/com/cardra/server/service/research/FallbackResearchDataAdapterTest.kt`
- 작업:
  - 외부 어댑터 성공/실패 시 API 응답 상태와 페이로드를 검증
  - fallback 경로에서 응답 스키마 유지 여부 확인
- Acceptance Criteria:
  - 성공/실패/fallback 3개 경로 테스트가 존재
  - 테스트에서 `items/summary/usage` 필드 호환성이 보장됨

### Task 3. ResearchService 메타 품질 강화
- 대상 파일:
  - `apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchService.kt`
  - `apps/server/src/test/kotlin/com/cardra/server/service/research/ResearchServiceTest.kt`
- 작업:
  - `usage.providerCalls`, `usage.cacheHit`, `usage.latencyMs` 규칙 확정
  - fallback 사용 시 로깅/메타 규칙 정의
- Acceptance Criteria:
  - `ResearchServiceTest`에서 메타 필드 계산/전달이 검증됨
  - fallback 시에도 API 계약 필드 누락이 없음

### Task 4. Card 생성 연동 준비
- 대상 파일:
  - `apps/server/src/main/kotlin/com/cardra/server/service/CardService.kt`
  - `apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchService.kt`
  - 필요 시 `apps/server/src/main/kotlin/com/cardra/server/dto/CardDtos.kt`
- 작업:
  - 카드 생성 시 리서치 결과를 소비할 수 있는 인터페이스 경계 정의
  - 기존 AgentAdapter 흐름과 충돌 없이 확장 포인트 추가
- Acceptance Criteria:
  - CardService에서 리서치 데이터 소비 경로가 코드상 명확함
  - 기존 카드 생성 테스트 회귀 없음

### Task 5. 최종 검증 + 문서 업데이트
- 대상 파일:
  - `apps/server/README.md`
  - `docs/pm/cardra-pm-overview.md`
- 작업:
  - 외부 리서치 어댑터 설정/실행/검증 방법 문서화
  - 진행 상태(완료/진행중/다음 작업) 갱신
- Acceptance Criteria:
  - 아래 명령이 모두 성공
    - `cd apps/server && ./gradlew test`
    - `cd apps/server && ./gradlew ktlintCheck`
    - `cd apps/server && ./gradlew build`
  - 문서에서 현재 구현 상태와 후속 과제가 일치

## Success Criteria
- Research adapter 기반 구조가 실패/복구 포함 운영 가능 수준으로 검증됨
- API 계약 회귀 없이 테스트 게이트를 통과함
- CardService 연동 준비가 코드/문서 양쪽에 반영됨
