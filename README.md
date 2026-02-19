# Cardra

Cardra는 키워드 기반 트렌드 카드뉴스 자동 생성 프로젝트입니다.

## 프로젝트 구조

- `apps/` : 실행 애플리케이션
- `packages/` : 공통 라이브러리/모듈
- `docs/` : 설계/기획 문서
- `infra/` : 배포/운영 스크립트
- `.claude/` : PM/코더 관련 지침(총괄)
- `.codex/` : 내용 생성/오케스트레이션 지침
- `.gemini/` : 리서치/디자인 지침
- `.agent-policies/` : 에이전트 라우팅/변경 규칙

## 서버 스펙

- **Backend:** Spring Boot (Kotlin)
- **Language:** Kotlin
- **Database:** PostgreSQL
- 실행 앱 위치: `apps/server/`

## 개발 시작

- 에이전트 우선순위: PM/총괄(Codex) → 리서치/디자인(Gemini) → 실행(Codex)
- 출력 형식: 카드뉴스 2~3장, 카드당 120~180자

## 운영 스크립트

```bash
cd infra

docker compose up -d

cd ../apps/server
./gradlew bootRun
```

## 현재 진행

- MCP: 서버 뼈대 완료
- 다음: 영구 캐시 정책/출처 필터/에이전트 호출 어댑터 붙이기
EOF