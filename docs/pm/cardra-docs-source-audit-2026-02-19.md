# Cardra 문서/자료 보강 감사 리포트 (2026-02-19)

## 범위
- 프로젝트 문서: `README.md`, `docs/stack.md`, `docs/pm/*.md`
- 실행/버전 기준: `apps/server/build.gradle.kts`, `apps/web/package.json`, `infra/docker-compose.yml`
- 외부 근거: 공식 문서/공식 레지스트리 URL

## 핵심 진단
1. 공식 출처 링크가 대부분 문서에 빠져 있어 검증 가능한 상태가 아니었음.
2. 버전 호환성(예: Java 21, Gradle, Spring Boot) 근거가 문서에 분산되어 있었음.
3. 프론트 일부 의존성은 최신 대비 격차가 크며(특히 React 18.2.0), "오래된 정보" 표식이 없었음.
4. Docker Compose 파일의 top-level `version`처럼 최신 명세에서 obsolete된 요소에 대한 안내가 없었음.

## 이번 턴 반영 내용
- `docs/stack.md`를 공식 URL 기반의 버전/호환성 중심 문서로 재작성.
- `docs/pm/README.md`에 문서 품질 기준(출처 URL, 버전 명시, 2년 이상 정보 플래그) 추가.
- 본 리포트(`cardra-docs-source-audit-2026-02-19.md`) 추가.

## 검증 결과 (공식 근거 기반)

### 1) 서버 스택 호환성
- Spring Boot 3.4 시스템 요구사항은 Java 17~23, Kotlin 1.7+를 명시.
- 현재 서버는 Java 21 + Kotlin 2.0.0 + Spring Boot 3.4.1 조합이므로 요구사항 범위 내.
- Gradle Java 호환성 표에서 Java 21로 Gradle 실행은 8.5+가 필요하며, 현재 wrapper 8.14는 충족.

### 2) 유지보수 수명/지원 정책
- Spring Boot OSS 지원 정책에서 3.4.x 지원 종료는 2026-06-30.
- PostgreSQL 16 지원 종료는 2028-11-13.

### 3) 프론트 최신성/구버전 표시 필요
- `react`/`react-dom` 현재 사용 `18.2.0` (배포일 2022-06-14)로, 2026-02-19 기준 2년 이상 경과.
- 최신 React는 `19.2.4` (npm 레지스트리 기준).
- `react-router-dom`은 현재 `6.22.3`, 최신 `7.13.0`.
- `vite`는 현재 `5.2.8`, 최신 `7.3.1`.
- `typescript`는 현재 `5.4.5`, 최신 `5.9.3`.

### 4) 운영 문서 최신 명세 반영 필요
- Docker Compose 명세에서 top-level `version`은 obsolete로 안내됨.

## 권장 후속 액션 (우선순위)
1. P1: PM 문서(`docs/pm/*.md`)에 "근거 링크" 섹션을 공통 템플릿으로 추가.
2. P1: 프론트 의존성 업그레이드 계획 수립.
3. P2: `infra/docker-compose.yml`에서 top-level `version` 제거 검토.
4. P2: 릴리스마다 `docs/stack.md` 갱신 기준일(YYYY-MM-DD) 업데이트.

## 공식 출처
- Spring Boot 3.4 System Requirements: https://docs.spring.io/spring-boot/3.4/system-requirements.html
- Spring Boot Project Page (current releases): https://spring.io/projects/spring-boot
- Spring Boot Supported Versions (OSS policy): https://github.com/spring-projects/spring-boot/wiki/Supported-Versions
- Gradle Java Compatibility: https://docs.gradle.org/current/userguide/compatibility.html#java
- PostgreSQL Versioning Policy: https://www.postgresql.org/support/versioning/
- Docker Compose (`version` obsolete): https://docs.docker.com/reference/compose-file/version-and-name/
- Vite v5 Guide (Node requirement): https://v5.vite.dev/guide/
- React 19 release notes: https://react.dev/blog/2024/12/05/react-19
- npm registry (latest version + release metadata):
  - https://registry.npmjs.org/react
  - https://registry.npmjs.org/react-dom
  - https://registry.npmjs.org/react-router-dom
  - https://registry.npmjs.org/%40tanstack%2Freact-query
  - https://registry.npmjs.org/vite
  - https://registry.npmjs.org/typescript
- Kotlin 2.2.20 release announcement: https://blog.jetbrains.com/kotlin/2025/09/kotlin-2-2-20-released/
