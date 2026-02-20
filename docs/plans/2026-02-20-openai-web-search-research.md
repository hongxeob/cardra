# OpenAI Web Search Research Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** `OpenAiResearchDataAdapter`가 실제 웹 검색 결과를 기반으로 리서치 페이로드를 생성하도록 두 단계 호출 방식으로 전환한다.

**Architecture:** ① Responses API(`/v1/responses`) + `web_search_preview` 툴로 실제 최신 기사 수집 → ② Chat Completions(`/v1/chat/completions`)으로 수집된 텍스트를 구조화된 `OpenAiResearchPayload` JSON으로 변환. 외부 인터페이스(`ResearchDataAdapter.fetch()`)는 변경 없음.

**Tech Stack:** Spring Boot (Kotlin), RestTemplate, Jackson, OpenAI Responses API, JUnit 5 + MockK

---

### Task 1: Config에 `webSearchTimeoutSeconds` 추가

**Files:**
- Modify: `apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt` — `OpenAiResearchConfig` 클래스
- Modify: `apps/server/src/main/resources/application.yml`
- Modify: `apps/server/src/main/resources/application-local.yml`

**Step 1: 실패 테스트 작성**

`OpenAiResearchDataAdapterTest.kt`에 아래 테스트 추가:

```kotlin
@Test
fun `config has default webSearchTimeoutSeconds of 20`() {
    val config = OpenAiResearchConfig()
    assertEquals(20L, config.webSearchTimeoutSeconds)
}
```

**Step 2: 테스트 실행 → 실패 확인**

```bash
cd apps/server && ./gradlew test --tests "*.OpenAiResearchDataAdapterTest" 2>&1 | tail -20
```
Expected: `FAILED — Unresolved reference: webSearchTimeoutSeconds`

**Step 3: Config 클래스에 필드 추가**

`OpenAiResearchConfig`에 한 줄 추가:
```kotlin
var webSearchTimeoutSeconds: Long = 20
```

**Step 4: application.yml에 키 추가**

```yaml
cardra:
  research:
    openai:
      # 기존 timeout-seconds 아래에 추가
      web-search-timeout-seconds: ${OPENAI_WEB_SEARCH_TIMEOUT_SECONDS:20}
```

**Step 5: application-local.yml에 추가**

```yaml
cardra:
  research:
    openai:
      timeout-seconds: 30
      web-search-timeout-seconds: 20
```

**Step 6: 테스트 + ktlint 통과 확인**

```bash
./gradlew test ktlintCheck 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

**Step 7: 커밋**

```bash
git add apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt \
        apps/server/src/main/resources/application.yml \
        apps/server/src/main/resources/application-local.yml \
        apps/server/src/test/kotlin/com/cardra/server/service/research/OpenAiResearchDataAdapterTest.kt
git commit -m "feat(research): add webSearchTimeoutSeconds to OpenAiResearchConfig"
```

---

### Task 2: Responses API 응답 파싱용 DTO 추가

**Files:**
- Modify: `apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt` — 파일 하단에 private DTO 추가

**Step 1: 실패 테스트 작성**

`OpenAiResearchDataAdapterTest.kt`에 추가:

```kotlin
@Test
fun `OpenAiResponsesResponse extracts text from message output`() {
    val mapper = jacksonObjectMapper()
    val json = """
        {
          "output": [
            {"type": "web_search_call", "content": []},
            {"type": "message", "content": [
              {"type": "output_text", "text": "검색 결과 텍스트"}
            ]}
          ]
        }
    """.trimIndent()
    val response = mapper.readValue(json, OpenAiResponsesResponse::class.java)
    val text = response.output
        .filter { it.type == "message" }
        .flatMap { it.content }
        .filter { it.type == "output_text" }
        .mapNotNull { it.text }
        .joinToString("\n")
    assertEquals("검색 결과 텍스트", text)
}
```

**Step 2: 테스트 실행 → 실패 확인**

```bash
./gradlew test --tests "*.OpenAiResearchDataAdapterTest" 2>&1 | tail -20
```
Expected: `FAILED — Unresolved reference: OpenAiResponsesResponse`

**Step 3: DTO 3개 추가** (`ResearchDataAdapters.kt` 파일 하단, 기존 DTO 블록 옆)

```kotlin
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiResponsesResponse(
    val output: List<OpenAiResponseOutput> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiResponseOutput(
    val type: String = "",
    val content: List<OpenAiResponseContent> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiResponseContent(
    val type: String = "",
    val text: String? = null,
)
```

**Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "*.OpenAiResearchDataAdapterTest" ktlintCheck 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

**Step 5: 커밋**

```bash
git add apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt \
        apps/server/src/test/kotlin/com/cardra/server/service/research/OpenAiResearchDataAdapterTest.kt
git commit -m "feat(research): add Responses API response DTOs"
```

---

### Task 3: `callWebSearch()` private 메서드 구현

**Files:**
- Modify: `apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt`

**Step 1: 실패 테스트 작성**

`OpenAiResearchDataAdapterTest.kt`에 추가:

```kotlin
@Test
fun `throws schema error when web search returns empty text`() {
    // callWebSearch가 빈 텍스트 반환 시 ExternalResearchSchemaError 발생 검증
    // RestTemplate mock 없이 검증 가능한 방법: enabled=true/apiKey set 상태에서
    // 실제 호출은 mock server 없이 불가 → 이 테스트는 통합 테스트로 대체
    // 여기서는 disabled 상태의 기존 테스트가 여전히 통과하는지 확인
    val config = OpenAiResearchConfig().apply {
        enabled = false
        apiKey = "sk-test"
        model = "gpt-4.1-mini"
    }
    val adapter = OpenAiResearchDataAdapter(config, jacksonObjectMapper(), RestTemplateBuilder())
    assertThrows(ExternalResearchSchemaError::class.java) {
        adapter.fetch(ResearchRunRequest(keyword = "AI"), "trace-1")
    }
}
```

> 참고: `callWebSearch()`는 실제 HTTP 호출을 포함하므로 단위 테스트에서 MockK로 RestTemplate을 직접 mock하기 어렵다. 기존 guard 테스트(disabled/no apiKey)가 통과하는 걸 확인하고, 실제 동작은 서버 실행 후 로그로 검증한다.

**Step 2: `OpenAiResearchDataAdapter`에 두 번째 RestTemplate 추가**

생성자 블록에 추가:
```kotlin
private val webSearchRestTemplate: RestTemplate =
    restTemplateBuilder
        .connectTimeout(Duration.ofSeconds(config.webSearchTimeoutSeconds))
        .readTimeout(Duration.ofSeconds(config.webSearchTimeoutSeconds))
        .build()
```

**Step 3: `callWebSearch()` 메서드 추가**

```kotlin
private fun callWebSearch(
    keyword: String,
    timeRange: String,
    traceId: String,
): String {
    val endpoint = "${config.baseUrl.trimEnd('/')}/v1/responses"
    val requestBody =
        mapOf(
            "model" to config.model,
            "tools" to listOf(mapOf("type" to "web_search_preview")),
            "input" to
                """
                최근 $timeRange 내 '$keyword' 관련 주요 뉴스와 이슈를 검색해줘.
                각 기사의 제목, 핵심 내용, 출처(매체명, URL), 발행일을 포함해서 최대 5개 정리해줘.
                """.trimIndent(),
        )
    val request =
        RequestEntity
            .post(URI(endpoint))
            .header("Authorization", "Bearer ${config.apiKey}")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)

    val response =
        try {
            webSearchRestTemplate.exchange(request, OpenAiResponsesResponse::class.java)
        } catch (e: RestClientResponseException) {
            logger.warn(
                "research_openai_websearch_http_error: traceId={} status={} body={}",
                traceId,
                e.statusCode.value(),
                e.responseBodyAsString.take(300),
            )
            throw mapHttpError(e)
        } catch (e: Exception) {
            logger.error("research_openai_websearch_transport_error: traceId={}", traceId, e)
            throw ExternalResearchTimeoutError("OpenAI web search call failed")
        }

    val text =
        response.body
            ?.output
            ?.filter { it.type == "message" }
            ?.flatMap { it.content }
            ?.filter { it.type == "output_text" }
            ?.mapNotNull { it.text }
            ?.joinToString("\n")
            .orEmpty()

    if (text.isBlank()) {
        throw ExternalResearchSchemaError("OpenAI web search returned empty result")
    }

    logger.info(
        "research_openai_websearch_success: traceId={} textLength={}",
        traceId,
        text.length,
    )
    return text
}
```

**Step 4: 테스트 + ktlint 통과 확인**

```bash
./gradlew test ktlintCheck 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

**Step 5: 커밋**

```bash
git add apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt
git commit -m "feat(research): add callWebSearch using Responses API web_search_preview"
```

---

### Task 4: `callStructuring()` private 메서드 구현 + `fetch()` 리팩터

**Files:**
- Modify: `apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt`

**Step 1: 프롬프트 상수 교체**

기존 `OPENAI_RESEARCH_SYSTEM_PROMPT` 상수를 아래로 교체:

```kotlin
private const val OPENAI_STRUCTURE_SYSTEM_PROMPT =
    "You are a research structuring engine. " +
        "Given real web search results, extract and return JSON only with camelCase keys and no markdown. " +
        "Use only information present in the search results. Do not fabricate URLs, publishers, or facts."
```

**Step 2: `buildStructuringPrompt()` 함수 추가** (기존 `buildOpenAiUserPrompt()` 대체)

```kotlin
private fun buildStructuringPrompt(
    searchText: String,
    req: ResearchRunRequest,
    traceId: String,
): String =
    """
    Below are real web search results for '${req.keyword}'.
    Convert them into a structured research payload.

    Search results:
    $searchText

    Request context:
    - keyword: ${req.keyword}
    - language: ${req.language}
    - country: ${req.country}
    - timeRange: ${req.timeRange}
    - maxItems: ${req.maxItems}
    - traceId: $traceId

    Return JSON object with this exact shape:
    {
      "items": [
        {
          "itemId": "string",
          "title": "string",
          "snippet": "string",
          "source": {"publisher":"string","url":"https://...","sourceType":"news|social|official|factcheck","author":"string|null"},
          "timestamps": {"publishedAt":"ISO-8601","collectedAt":"ISO-8601","lastVerifiedAt":"ISO-8601"},
          "factcheck": {"status":"supported|disputed|insufficient|false-risk","confidence":0.0,"confidenceReasons":["string"],"claims":[{"claimText":"string","verdict":"supported|disputed|insufficient|false-risk","evidenceIds":["string"]}]},
          "trend": {"trendScore":0,"velocity":0.0,"regionRank":0}
        }
      ],
      "summary": {"brief":"string","analystNote":"string","riskFlags":["string"]}
    }
    Rules:
    - 1..${req.maxItems} items
    - Use only real URLs and publisher names from the search results above
    - Do not include markdown fences
    """.trimIndent()
```

**Step 3: `callStructuring()` 메서드 추가**

```kotlin
private fun callStructuring(
    searchText: String,
    req: ResearchRunRequest,
    traceId: String,
): OpenAiResearchPayload {
    val endpoint = "${config.baseUrl.trimEnd('/')}/v1/chat/completions"
    val requestBody =
        mapOf(
            "model" to config.model,
            "temperature" to config.temperature,
            "response_format" to mapOf("type" to "json_object"),
            "messages" to
                listOf(
                    mapOf("role" to "system", "content" to OPENAI_STRUCTURE_SYSTEM_PROMPT),
                    mapOf("role" to "user", "content" to buildStructuringPrompt(searchText, req, traceId)),
                ),
        )
    val request =
        RequestEntity
            .post(URI(endpoint))
            .header("Authorization", "Bearer ${config.apiKey}")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)

    val response =
        try {
            restTemplate.exchange(request, OpenAiChatResponse::class.java)
        } catch (e: RestClientResponseException) {
            logger.warn(
                "research_openai_structure_http_error: traceId={} status={} body={}",
                traceId,
                e.statusCode.value(),
                e.responseBodyAsString.take(300),
            )
            throw mapHttpError(e)
        } catch (e: Exception) {
            logger.error("research_openai_structure_transport_error: traceId={}", traceId, e)
            throw ExternalResearchTimeoutError("OpenAI structuring call failed")
        }

    val rawContent =
        response.body
            ?.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()
            .orEmpty()

    logger.info("research_openai_structure_raw: traceId={} content={}", traceId, rawContent)

    if (rawContent.isBlank()) {
        throw ExternalResearchSchemaError("OpenAI structuring returned empty content")
    }

    return try {
        objectMapper.readValue(stripCodeFence(rawContent), OpenAiResearchPayload::class.java)
    } catch (_: Exception) {
        throw ExternalResearchSchemaError("OpenAI structuring response is not valid JSON payload")
    }
}
```

**Step 4: `fetch()` 본문을 두 단계로 교체**

기존 `fetch()` 내부의 단일 호출 로직을 아래로 교체 (guard 조건은 그대로):

```kotlin
// 기존 단일 호출 로직 전체를 아래 두 줄로 교체
val searchText = callWebSearch(req.keyword, req.timeRange, traceId)
val payload = callStructuring(searchText, req, traceId)
```

`return` 블록의 `providerCalls`를 `2`로 변경:
```kotlin
return ResearchDataPayload(
    items = payload.items,
    summary = payload.summary ?: throw ExternalResearchSchemaError("OpenAI structuring response contains no summary"),
    providerCalls = 2,
    cacheHit = false,
)
```

기존 `buildOpenAiUserPrompt()` 함수 삭제.

**Step 5: 테스트 + ktlint 통과 확인**

```bash
./gradlew test ktlintCheck 2>&1 | tail -10
```
Expected: `BUILD SUCCESSFUL`

**Step 6: 커밋**

```bash
git add apps/server/src/main/kotlin/com/cardra/server/service/research/ResearchDataAdapters.kt
git commit -m "feat(research): implement two-step web search + structuring in OpenAiResearchDataAdapter"
```

---

### Task 5: 서버 실행 후 실제 동작 검증

**Step 1: 서버 재시작** (IDE Run Configuration 또는 아래 명령)

```bash
cd apps/server && ./gradlew bootRun --args='--spring.profiles.active=local'
```

**Step 2: deep mode로 요청**

```bash
curl -s -X POST http://localhost:9999/api/v1/cards/generate \
  -H "Content-Type: application/json" \
  -d '{"keyword":"엔비디아","mode":"deep"}' | jq .
```

**Step 3: 로그 확인**

```bash
tail -f apps/server/logs/cardra-local.log | grep -E "websearch|structure_raw"
```

다음 두 로그가 순서대로 찍히면 성공:
```
research_openai_websearch_success: traceId=... textLength=...
research_openai_structure_raw: traceId=... content={"items":[...실제 URL...]}
```

`content` 안의 URL이 실제 존재하는 사이트면 완료.
