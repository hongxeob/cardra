# Cardra Server

## Backend Spec (v1)
- Framework: Spring Boot (Kotlin)
- Language: Kotlin
- DB: PostgreSQL

## API
- `POST /api/v1/cards/generate` : 카드 생성 요청
- `GET /api/v1/cards/{id}` : 생성 결과 조회
- `GET /api/v1/health` : 헬스체크

## 실행 가이드
1. PostgreSQL + 서버 동시 실행
   ```bash
   cd infra
   cp .env.example .env  # 필요 시 값 수정
   docker compose up --build -d
   ```
2. 서버 단일 실행
   ```bash
   cd apps/server
   ./gradlew bootRun
   ```

## 정책
- 카드 1장당 120~180자(현재 MVP는 40~220자로 보강)
- 출처 최소 2개 이상
- 2~3장 카드 기본 구성
