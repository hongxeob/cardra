# Cardra Server

## Backend Spec (v1)
- Framework: Spring Boot (Kotlin)
- Language: Kotlin
- DB: PostgreSQL

## 목표
- 키워드 입력 기반 카드뉴스 생성 파이프라인 API
- 에이전트(리서치/작성/렌더) 오케스트레이션
- 생성 이력 저장 및 추천 후보 산출

## API
- `POST /api/v1/cards/generate` : 카드 생성 요청
- `GET /api/v1/cards/{id}` : 생성 결과 조회
- `GET /api/v1/health` : 헬스체크

## 실행 가이드
1. PostgreSQL 실행
   ```bash
   cd infra
   docker compose up -d
   ```
2. 서버 실행
   ```bash
   cd apps/server
   ./gradlew test
   ./gradlew bootRun
   ```

## 정책
- 카드 1장당 120~180자
- 출처 최소 2개 이상
- 2~3장 카드 기본 구성
