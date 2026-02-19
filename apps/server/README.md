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

## 실제 AI 연동 테스트 (OpenAI)

`ResearchService`는 아래 우선순위로 데이터를 조회합니다.
1. OpenAI 직접 호출 (`cardra.research.openai`)
2. 외부 Research API (`cardra.research.external`)
3. 내장 Stub fallback

### 필요한 키/플랫폼
- 플랫폼: OpenAI API
- 필요한 것: `OPENAI_API_KEY` (OpenAI API key)
- 발급 위치: OpenAI 플랫폼의 API keys 페이지

### 로컬에서 키 연결
```bash
cd apps/server
export OPENAI_API_KEY="sk-..."
export CARDRA_RESEARCH_OPENAI_ENABLED=true
# 필요 시 모델 교체
export OPENAI_MODEL="gpt-4.1-mini"
```

### 실행 및 호출 확인
```bash
cd apps/server
./gradlew bootRun
```

별도 터미널:
```bash
curl -s -X POST http://localhost:9999/api/v1/research/run \
  -H 'Content-Type: application/json' \
  -d '{
    "keyword":"AI 에이전트",
    "language":"ko",
    "country":"KR",
    "timeRange":"24h",
    "maxItems":3
  }'
```

응답에서 `items`, `summary`, `usage` 필드가 채워지면 OpenAI 경로 또는 fallback 경로가 정상 동작합니다.

## 이미지 생성 AI 테스트 (OpenAI / Gemini)

이미지 생성 API:
- `POST /api/v1/images/generate`
- `GET /api/v1/images/providers/status` (키/활성 상태 점검)

필요 환경 변수 (OpenAI 사용):
```bash
cd apps/server
export OPENAI_API_KEY="sk-..."
export CARDRA_IMAGE_PROVIDER="openai"
export CARDRA_IMAGE_OPENAI_ENABLED=true
export OPENAI_IMAGE_MODEL="gpt-image-1"
```

필요 환경 변수 (Gemini nano banana 사용):
```bash
cd apps/server
export GEMINI_API_KEY="..."
export CARDRA_IMAGE_PROVIDER="gemini"
export CARDRA_IMAGE_GEMINI_ENABLED=true
export GEMINI_IMAGE_MODEL="gemini-2.5-flash-image"
```

호출 예시:
```bash
curl -s -X POST http://localhost:9999/api/v1/images/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "prompt":"미래 도시와 AI 에이전트, 시네마틱 조명",
    "size":"1024x1024",
    "provider":"gemini"
  }'
```

`provider`는 선택입니다.
- 미지정: `CARDRA_IMAGE_PROVIDER` 설정값 사용
- 지정 가능값: `openai`, `gemini`, `nano-banana` (`nano banana`도 허용)

상태 점검 예시:
```bash
curl -s http://localhost:9999/api/v1/images/providers/status
```

응답:
- OpenAI 성공 시 `provider=openai` 및 `imageBase64` 또는 `imageUrl`
- Gemini 성공 시 `provider=gemini` 및 `imageBase64`
- 실패/미설정 시 fallback으로 `provider=stub`, `imageUrl` 반환

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
