# Cardra

Cardra는 키워드 입력만으로 트렌드 이슈를 카드뉴스로 생성하는 서비스입니다.  
단순 API 호출이 아니라, **AI 모델/어댑터/폴백/잡 실행**을 조합해 결과 품질과 안정성을 함께 다루는 오케스트레이션 구조를 목표로 합니다.

## 프로젝트 소개

Cardra는 "콘텐츠 제작 시간을 줄이면서도 근거 기반 품질을 유지"하는 것을 목표로 시작한 AI 제품입니다.  
사용자는 키워드만 입력하면, 서버가 리서치 파이프라인과 카드 생성 파이프라인을 오케스트레이션해 카드뉴스 결과를 반환합니다.

핵심은 모델 1개를 단순 호출하는 구조가 아니라, 운영 환경에서 필요한 fallback, 비동기 작업 제어, API 계약 유지, 테스트/빌드 검증까지 포함한 **제품형 AI 백엔드**를 만드는 데 있습니다.

## 이 프로젝트가 보여주는 점 (AI Orchestration)

- 카드 생성 오케스트레이션: `quick` / `deep` 모드 분기
- 리서치 오케스트레이션: OpenAI/External 어댑터 + fallback(stub) 체인
- 이미지 오케스트레이션: `openai`, `gemini`(=`nano-banana` 별칭 포함) 라우팅 + fallback
- 비동기 잡 오케스트레이션: 연구 작업 생성/상태/결과/취소 API + running future 정리
- 에이전트 추상화: `AgentAdapter` 기반으로 공급자 교체 가능한 구조

채용/지원 관점에서 보면, 이 레포는 "AI를 붙였다" 수준이 아니라  
**여러 AI 경로를 운영 가능한 제품 흐름으로 설계/검증한 경험**을 설명하기 좋은 형태입니다.

## 개발 방식 (Vibe Coding + AI)

- Vibe Coding 방식으로 빠르게 가설을 실험하되, 결과물은 테스트/린트/빌드 기준으로 수렴
- 멀티 에이전트 오케스트레이션으로 탐색-구현-검증을 병렬화해 개발 리드타임 단축
- "아이디어를 빠르게 형태로 만들고, 품질 게이트로 안정화"하는 AI 네이티브 개발 프로세스 적용
- 실제 코드 레벨에서는 adapter/fallback/contract-test 중심으로 운영 안정성을 우선

## 프로젝트 구조

- `apps/server` : Spring Boot(Kotlin) API 서버
- `apps/web` : React/Vite 프론트엔드
- `infra` : 로컬 인프라 실행 스크립트(docker compose)
- `docs` : 기획/설계/작업 문서
- `scripts` : 개발 보조 스크립트(OMX, hooks 등)

## 기술 스택

- Backend: Kotlin, Spring Boot, JPA, PostgreSQL
- Frontend: React, TypeScript, Vite
- Docs/API: springdoc-openapi, Swagger UI
- Tooling: Gradle, ktlint, OMX(oh-my-codex) 기반 멀티 에이전트 워크플로우

## 로컬 실행

### 1) 인프라 실행

```bash
cd infra
cp .env.example .env
docker compose up --build -d
```

### 2) 서버 실행

```bash
cd apps/server
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
./gradlew bootRun
```

### 3) 웹 실행

```bash
cd apps/web
npm install
npm run dev
```

## API 문서

- Swagger UI: `http://localhost:9999/swagger-ui.html`
- OpenAPI JSON: `http://localhost:9999/v3/api-docs`

## 품질 게이트

```bash
cd apps/server
./gradlew test
./gradlew ktlintCheck
./gradlew build
```

## Git Hook 설정

```bash
./scripts/setup-hooks.sh
```

pre-commit / pre-push에서 서버 `ktlintCheck`가 실행되도록 맞춰져 있습니다.

## 현재 진행 상태 (2026-02-21 기준)

- 완료: ResearchService 외부 데이터 어댑터 기반 전환(Primary/Fallback)
- 완료: 서버 API 계약 유지 및 테스트/빌드 파이프라인 통과
- 완료: CORS 설정 추가, 비동기 잡 `runningFutures` 정리 보강
- 완료: Swagger 문서 경로 정리 및 API 태그/설명 보강
- 진행 중: 실제 운영 키 기준 end-to-end 호출 검증 및 배포 환경값 정리

## OMX 최소 사용 예

```bash
./scripts/omx.sh team 3:executor "cardra 서버/웹 개선 항목을 병렬 점검해줘"
./scripts/omx.sh team status
./scripts/omx.sh team shutdown <team-name>
```

```bash
./scripts/omx.sh --high
./scripts/omx.sh --xhigh
```

주의: `--madmax`는 승인/샌드박스 우회 모드이므로 신뢰 환경에서만 사용하세요.
