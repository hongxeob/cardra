# cardra-server AGENTS

## Scope
This folder is the backend server for `cardra`.

## Stack
- Kotlin + Spring Boot (JVM)
- PostgreSQL
- Gradle (wrapper)
- JDK 21 required

## Runtime
- Server app root: `apps/server`
- Run tests: `./gradlew test`
- Style check: `./gradlew ktlintCheck`
- Format: `./gradlew ktlintFormat`

## Quality Gate
Before commit/push, run:
1. `./gradlew test`
2. `./gradlew ktlintCheck`

## Structure
- `api/` : HTTP controllers
- `service/` : business logic
- `service/agent/` : agent interface + implementations
- `dto/` : request/response DTOs
- `domain/` : JPA entities & enums
- `repository/` : persistence interfaces
- `exception/` : centralized exception handler

## Conventions
- Prefer dependency inversion for external adapters (`AgentAdapter` + fallback).
- Keep response body in policy range (3 cards, clear source metadata).
- Keep tests for service + controller paths when logic changes.
- Commit formatting changes if `ktlintFormat` modifies files.

## Repo-wide hooks
- pre-commit/pre-push are configured to run ktlintFormat + ktlintCheck from `apps/server`.