# Cardra 추천 엔진 MVP (벡터 DB) - PM 제안

## 목표
사용자 히스토리를 기반으로 다음 키워드 후보를 빠르게 추천.

## 범위
- user-event 기반 임베딩 생성
- 검색/클릭/조회 신호 반영
- 벡터 유사도 + 인기/트렌드 랭킹

## 현재 구현 상태
- `POST /api/v1/recommend/keywords`: 현재 in-memory user-event 기반으로 간이 추천 생성
- `POST /api/v1/recommend/events`: 사용자 이벤트 적재 API (메모리 목록)
- 응답에 `fallbackUsed`, `fallbackReason`, `strategy` 추가 (`PERSONALIZED/SESSION_CONTEXT/GLOBAL_POPULAR`)
- 컨트롤러 요청 DTO 유효성 검증(`userId`, `events`, 이벤트 필드)
- 테스트 추가: 컨트롤러 테스트 + 서비스 단위 테스트 반영

## 최소 스키마 (최종 목표)

### `user_event_history`
- user_id, session_id, event_type(search/click/view), keyword, event_ts
- event_type 가중치, dwell_ms, metadata(jsonb)
- 인덱스: `(user_id, event_ts desc)`

### `user_profile_embedding_meta`
- user_id, last_embedding_ts, history_window_days, version, quality_score

### 벡터 컬렉션
- `keyword_vectors`: keyword 임베딩 + payload(언어, 카테고리, 인기도)
- `user_context_vectors`: 사용자 최근 히스토리 임베딩

## API
- `POST /v1/recommend/keywords`
- `POST /v1/events`
- `POST /v1/admin/rebuild-embeddings`(운영용)

예시 요청
```json
{
  "user_id": "u123",
  "current_query": "러닝화",
  "category_id": "sports",
  "locale": "ko-KR",
  "limit": 10
}
```

예시 응답
```json
{
  "request_id": "req-1",
  "user_id": "u123",
  "candidates": [
    { "keyword": "아식스 젤 카야노", "score": 0.892, "source": "vector_personalized" }
  ],
  "fallback_used": false,
  "model_version": "emb-v1.3"
}
```

## 추천 점수(초기)
`0.65*cosine + 0.20*popularity + 0.10*recency_trend + 0.05*diversity`

## 폴백 순서
1. 사용자 벡터 없음: 쿼리 중심 + 인기 키워드
2. 벡터 검색 장애: 캐시 추천
3. 임베딩 장애: 룰 기반(동의어/최근키워드 확장)

## 다음 단계
- 사용자 이벤트를 RDB/캐시 영속화(`user_event_history`)로 전환
- 후보 생성기와 랭킹 계산을 분리해 추천 품질과 테스트가 분리되도록 개선
- 벡터 서비스 도입 전 `modelVersion`과 fallbackReason 사양 고정
