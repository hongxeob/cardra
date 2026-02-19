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

## 코드 스타일
- `ktlint` 적용: `./gradlew ktlintCheck`
- 추천: 커밋 전후 `ktlintCheck` 통과 필수

## Java 21 가이드
- 이 모듈은 **JDK 21** 기준입니다.
- 실행 전:
  ```bash
  export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
  ./gradlew -version
  ```
- `./gradlew`는 Java 21이 아닌 환경에서 실행되면 실패하도록 가드가 걸려 있습니다.

## Agent Adapter 레이어
- 카드 본문 생성은 `service.agent.AgentAdapter` 인터페이스로 분리
- 기본 구현은 `MockAgentAdapter` (stub)로 제공
- 추후 실제 Gemini/Claude 연동은 별도 구현체를 추가해 DI 교체

## 테스트
```bash
cd apps/server
./gradlew test
```
