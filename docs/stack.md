# Cardra Technical Stack (Verified: 2026-02-19)

## Backend (현재 고정 버전)
- Spring Boot: `3.4.1` (`apps/server/build.gradle.kts`)
- Kotlin: `2.0.0` (`apps/server/build.gradle.kts`)
- Java: `21` (`apps/server/build.gradle.kts`)
- Gradle Wrapper: `8.14` (`apps/server/gradle/wrapper/gradle-wrapper.properties`)
- PostgreSQL: `16` (`infra/docker-compose.yml`)

## Frontend (현재 고정 버전)
- React: `18.2.0` (`apps/web/package.json`)
- React DOM: `18.2.0` (`apps/web/package.json`)
- React Router DOM: `6.22.3` (`apps/web/package.json`)
- TanStack React Query: `5.28.4` (`apps/web/package.json`)
- Vite: `5.2.8` (`apps/web/package.json`)
- TypeScript: `5.4.5` (`apps/web/package.json`)

## 호환성/최신성 점검 요약
- Spring Boot 3.4 계열 요구사항(Java 17-23, Kotlin 1.7+) 기준에서 현재 서버 조합은 호환 가능.
- Java 21로 Gradle 실행 시 Gradle 8.5+가 필요하며, 현재 Wrapper 8.14는 기준 충족.
- PostgreSQL 16은 지원 종료일이 2028-11-13으로 명시되어 단기 운영 기준 안전.
- 프론트는 React 18.2.0(2022-06 릴리스) 사용 중으로, 최신 React 19 계열 대비 2년 이상 경과한 정보가 포함됨.
- `infra/docker-compose.yml`의 top-level `version` 필드는 Compose 최신 명세에서 obsolete로 안내됨.

## 참고 기준(공식 문서)
- Spring Boot 3.4 시스템 요구사항: [docs.spring.io](https://docs.spring.io/spring-boot/3.4/system-requirements.html)
- Spring Boot 현재 릴리스 목록: [spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
- Spring Boot OSS 지원 정책: [github.com/spring-projects/spring-boot/wiki/Supported-Versions](https://github.com/spring-projects/spring-boot/wiki/Supported-Versions)
- Gradle Java 호환성 매트릭스: [docs.gradle.org/current/userguide/compatibility.html#java](https://docs.gradle.org/current/userguide/compatibility.html#java)
- PostgreSQL 지원 버전 정책: [postgresql.org/support/versioning](https://www.postgresql.org/support/versioning/)
- Docker Compose file reference (`version` obsolete): [docs.docker.com/reference/compose-file/version-and-name](https://docs.docker.com/reference/compose-file/version-and-name/)
- Vite v5 시작 가이드(Node 요구사항): [v5.vite.dev/guide](https://v5.vite.dev/guide/)
- React 19 릴리스: [react.dev/blog/2024/12/05/react-19](https://react.dev/blog/2024/12/05/react-19)
- React/웹 패키지 최신 버전(레지스트리):
  - [registry.npmjs.org/react](https://registry.npmjs.org/react)
  - [registry.npmjs.org/react-dom](https://registry.npmjs.org/react-dom)
  - [registry.npmjs.org/react-router-dom](https://registry.npmjs.org/react-router-dom)
  - [registry.npmjs.org/%40tanstack%2Freact-query](https://registry.npmjs.org/%40tanstack%2Freact-query)
  - [registry.npmjs.org/vite](https://registry.npmjs.org/vite)
  - [registry.npmjs.org/typescript](https://registry.npmjs.org/typescript)
