# Cardra 리서처 파이프라인 (Gemini 3 기반) 문서

## 목표
`keyword` 입력 기준으로 최신 이슈를 수집·팩트체크·요약해서 카드 생성에 바로 쓸 수 있는 구조화 데이터로 반환.

## 소스 제안 (운영 우선순위)
1. 실시간: 뉴스/트렌드/소셜(이슈 발굴)
2. 신뢰도 우선: 공식기관·통신사
3. 보조: 팩트체크 센터

## 처리 흐름(요약)
`ingest -> normalize -> claim 추출 -> 근거수집 -> fact-check 점수화 -> Gemini 요약 -> output`

## 필수 메타데이터
- source: `publisher, url, source_type(news/social/official/factcheck), author`
- timestamp: `published_at, collected_at, last_verified_at`
- geo/lang: `country, language`
- credibility: `source_authority_score, content_reliability_score, confidence`
- provenance: `trace_id, claim_id, evidence_ids, pipeline_version, model_version`

## 최소 응답 스키마 (현재 구현 반영)
```json
{
  "trace_id": "string",
  "query": { "keyword": "string", "language": "ko", "country": "KR", "time_range": "24h" },
  "generated_at": "ISO-8601",
  "items": [
    {
      "item_id": "string",
      "title": "string",
      "snippet": "string",
      "source": { "publisher": "string", "url": "uri", "source_type": "news|social|official|factcheck" },
      "timestamps": { "published_at": "ISO-8601", "collected_at": "ISO-8601", "last_verified_at": "ISO-8601" },
      "factcheck": {
        "status": "supported|disputed|insufficient|false-risk",
        "confidence": 0.0,
        "claims": [{ "claim_text": "string", "verdict": "supported", "evidence_ids": ["string"] }
        ]
      },
      "trend": { "trend_score": 0, "velocity": 0.0 }
    }
  ],
  "summary": { "brief": "string", "analyst_note": "string", "risk_flags": ["string"] },
  "error": { "code": "string", "message": "string", "retryable": true, "retry_after": 5, "trace_id": "uuid", "usage": {"provider_calls": 1, "latency_ms": 0, "cache_hit": false} }
}
```

## API 계약(우선순위)
- `POST /v1/research/run` : 동기 실행(소량), 리턴 포함
- `POST /v1/research/jobs`, `GET /v1/research/jobs/{job_id}` : 비동기 실행
- `GET /v1/research/jobs/{job_id}/result` : 결과 및 재시도 힌트 조회
- `POST /v1/research/jobs/{job_id}/cancel` : 실행 취소
- `POST /v1/factcheck/claims` : 단건 claim 판정(구현 대기)

## 코드 반영 체크
- `search keyword`가 들어오면 `CardService`에서 바로 mock를 호출하지 않고 `Research` 기반 DTO 계약으로 연결 가능한 구조로 분할
- 현재 `ResearchService`는 stub 동작이며 `ResearchJobService`가 비동기 큐+상태+idempotency를 보유
- 실패 시 에러 포맷은 `error.code/retryable/retry_after/trace_id/usage`를 노출하도록 정합화
- 카드는 현재 실패/폴백 시에도 구조를 안정적으로 반환하도록 정책 단계 준비 중
- 다중 인스턴스 장애 대응: 현재 in-memory 상태라 재시작·스케일아웃 시 상태 소실이 있어, 우선순위 1단계 과제

## 다음 스텝
1. 리서치 Job store를 영속화(Redis/JPA)로 전환: 상태/생성시각/TTL 관리
2. 외부 연구/팩트체크 adapter 실제 연결
3. cache-hit 결과를 재요청/재시도 정책에 통합
4. 카드 서비스와 research contract 테스트 1:1 연결 강화
