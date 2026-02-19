# Cardra Agent Orchestrator Spec (PM+코더용)

## 목적
외부 에이전트 연동을 “한 번의 큰 변경”이 아닌, 최소 변경으로 분할하여 안정적으로 붙인다.

## 권장 아키텍처 (기존 유지)

### 기존 계층
- API 라우트: `CardController`, `HealthController`
- UseCase/Service: `CardService`

### 새로 추가할 얇은 계층
1. **Agent Orchestrator**: 호출 순서, timeout/retry/fallback 결정
2. **Agent Adapter**: provider별 REST 호출/파싱
3. **Schema & Validation**: 요청/응답 구조 검증
4. **Observability**: trace/latency/fallbackReason 로깅

## 표준 요청/응답 계약(최소)

### Agent Request (Internal)
```json
{
  "requestId": "uuid",
  "traceId": "uuid",
  "agent": { "name": "research|planner|writer", "version": "v1" },
  "task": {
    "type": "generate|summarize|factcheck",
    "input": {},
    "constraints": { "timeoutMs": 8000, "maxTokens": 2000 }
  },
  "context": { "userId": "string", "locale": "ko-KR" },
  "meta": { "requestedAt": "ISO-8601" }
}
```

### Agent Response (Internal)
```json
{
  "requestId": "uuid",
  "traceId": "uuid",
  "status": "ok|partial|error|fallback",
  "result": {
    "output": {},
    "usage": { "promptTokens": 0, "completionTokens": 0 }
  },
  "error": { "code": "TIMEOUT|RATE_LIMIT|INVALID_SCHEMA|UPSTREAM_5XX|UNKNOWN", "retryable": true, "message": "string" },
  "fallback": { "used": false, "strategy": "none|cache|secondary-agent|rule-based|graceful-degrade", "reason": "string" },
  "meta": { "latencyMs": 0, "respondedAt": "ISO-8601", "provider": "openai|local|custom" }
}
```

## 실패 우선순위 (요약)
1. `INVALID_SCHEMA` → 즉시 실패 처리(재시도 불필요)
2. timeout/429/5xx → 재시도 1~2회 + 지수 backoff
3. 실패 시 fallback: 경량 대체 경로, 캐시, 규칙 기반
4. `partial` 처리 시 누락 필드 명시, 사용자 표시에서 오해 없게

## Kotlin 코드 관점 반영 포인트
- `agent` 패키지에서 외부 호출 로직을 `AgentAdapter`로 고정
- `ExternalAgentAdapter`는 1개 Provider만 붙이더라도 위 계약 DTO 기반으로 응답 래핑
- Fallback은 현재 있는 `FallbackAgentAdapter` 또는 신규 안전 템플릿을 사용
- 예외를 domain error 로 매핑해 `CardService`는 비즈니스 예외만 알도록

## 검증 포인트
- status별(ok/partial/fallback/error) 분기 테스트 작성
- traceId/latency 로그 존재 확인
- timeout/429/invalid schema 시 재시도/fallback 동작 확인