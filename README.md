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

## 운영 스크립트

```bash
cd /Users/hongxeob/Desktop/project/cardra/infra
cp .env.example .env
# 값 수정 후

docker compose up --build -d
```

## 현재 진행

- 서버 MVP 뼈대 구축 완료
- 에이전트 운영 문서 정합성 구성
- 다음: 에이전트 호출 어댑터(리서치/작성/디자인), DB 스키마 검증, API 계약 테스트
