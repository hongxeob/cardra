# Cardra Server

## Backend Spec (v1)
- Framework: Spring Boot (Kotlin)
- Language: Kotlin
- DB: PostgreSQL

## 목표
- 키워드 입력 기반 카드뉴스 생성 파이프라인 API
- 에이전트(리서치/작성/렌더) 연동 오케스트레이션
- 생성 이력, 사용자 히스토리 저장 및 추천 후보 산출

## 기본 엔드포인트(초기)
- `POST /api/v1/cards/generate` : 키워드 기반 카드 생성 요청
- `GET /api/v1/cards/{id}` : 생성 작업/결과 조회
- `GET /api/v1/health` : 헬스체크

## 정책
- 카드 1장당 120~180자 제약
- 출처 최소 2개 이상 필수
- 생성 규칙(2~3장, 키워드 기반)
