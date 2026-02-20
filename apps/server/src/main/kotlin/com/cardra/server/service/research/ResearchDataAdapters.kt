package com.cardra.server.service.research

import com.cardra.server.dto.ResearchItemDto
import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchSummaryDto
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.Duration
import java.time.Instant

data class ResearchDataPayload(
    val items: List<ResearchItemDto>,
    val summary: ResearchSummaryDto,
    val providerCalls: Int = 1,
    val cacheHit: Boolean = false,
)

interface ResearchDataAdapter {
    fun fetch(
        req: ResearchRunRequest,
        traceId: String,
    ): ResearchDataPayload
}

sealed class ExternalResearchError(
    message: String,
) : RuntimeException(message)

class ExternalResearchTimeoutError(
    message: String,
) : ExternalResearchError(message)

class ExternalResearchRateLimitError(
    message: String,
) : ExternalResearchError(message)

class ExternalResearchUpstreamError(
    message: String,
) : ExternalResearchError(message)

class ExternalResearchSchemaError(
    message: String,
) : ExternalResearchError(message)

@Component
@ConfigurationProperties(prefix = "cardra.research.openai")
class OpenAiResearchConfig {
    var enabled: Boolean = false
    var baseUrl: String = "https://api.openai.com"
    var model: String = "gpt-4.1-mini"
    var apiKey: String = ""
    var timeoutSeconds: Long = 20
    var webSearchTimeoutSeconds: Long = 20
    var temperature: Double = 0.2
}

@Component
@ConfigurationProperties(prefix = "cardra.research")
class ResearchFallbackConfig {
    var allowStubFallback: Boolean = true
}

@Component("openAiResearchDataAdapter")
class OpenAiResearchDataAdapter(
    private val config: OpenAiResearchConfig,
    private val objectMapper: ObjectMapper,
    restTemplateBuilder: RestTemplateBuilder,
) : ResearchDataAdapter {
    private val logger = LoggerFactory.getLogger(OpenAiResearchDataAdapter::class.java)
    private val restTemplate: RestTemplate =
        restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .readTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .build()

    override fun fetch(
        req: ResearchRunRequest,
        traceId: String,
    ): ResearchDataPayload {
        if (!config.enabled) {
            throw ExternalResearchSchemaError("OpenAI research adapter is disabled")
        }
        if (config.apiKey.isBlank()) {
            throw ExternalResearchSchemaError("OPENAI_API_KEY is required when OpenAI research adapter is enabled")
        }
        if (config.model.isBlank()) {
            throw ExternalResearchSchemaError("OpenAI model is required when OpenAI research adapter is enabled")
        }
        logger.info(
            "research_openai_request: traceId={} keyword={} model={} maxItems={}",
            traceId,
            req.keyword,
            config.model,
            req.maxItems,
        )

        val endpoint = "${config.baseUrl.trimEnd('/')}/v1/chat/completions"
        val requestBody =
            mapOf(
                "model" to config.model,
                "temperature" to config.temperature,
                "response_format" to mapOf("type" to "json_object"),
                "messages" to
                    listOf(
                        mapOf(
                            "role" to "system",
                            "content" to OPENAI_RESEARCH_SYSTEM_PROMPT,
                        ),
                        mapOf(
                            "role" to "user",
                            "content" to buildOpenAiUserPrompt(req, traceId),
                        ),
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
                    "research_openai_http_error: traceId={} status={} statusText={} body={}",
                    traceId,
                    e.statusCode.value(),
                    e.statusText,
                    e.responseBodyAsString.take(300),
                )
                throw mapHttpError(e)
            } catch (e: Exception) {
                logger.error("research_openai_transport_error: traceId={}", traceId, e)
                throw ExternalResearchTimeoutError("OpenAI research call failed")
            }

        if (!response.statusCode.is2xxSuccessful) {
            throw ExternalResearchUpstreamError("OpenAI returned non-success status: ${response.statusCode}")
        }

        val rawContent =
            response.body
                ?.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.trim()
                .orEmpty()

        logger.info("research_openai_raw_response: traceId={} content={}", traceId, rawContent)

        if (rawContent.isBlank()) {
            throw ExternalResearchSchemaError("OpenAI returned empty content")
        }

        val payload =
            try {
                objectMapper.readValue(stripCodeFence(rawContent), OpenAiResearchPayload::class.java)
            } catch (_: Exception) {
                throw ExternalResearchSchemaError("OpenAI response is not valid research JSON payload")
            }

        if (payload.items.isEmpty()) {
            throw ExternalResearchSchemaError("OpenAI response contains no items")
        }
        if (payload.summary == null) {
            throw ExternalResearchSchemaError("OpenAI response contains no summary")
        }
        logger.info(
            "research_openai_success: traceId={} items={} providerCalls={}",
            traceId,
            payload.items.size,
            1,
        )

        return ResearchDataPayload(
            items = payload.items,
            summary = payload.summary,
            providerCalls = 1,
            cacheHit = false,
        )
    }

    private fun mapHttpError(e: RestClientResponseException): ExternalResearchError {
        val status = e.statusCode.value()
        return when {
            status == 429 -> {
                ExternalResearchRateLimitError("OpenAI call failed: ${e.statusCode} ${e.statusText}")
            }

            status in 500..599 -> {
                ExternalResearchUpstreamError("OpenAI call failed: ${e.statusCode} ${e.statusText}")
            }

            else -> {
                ExternalResearchSchemaError("OpenAI request failed: ${e.statusCode} ${e.statusText}")
            }
        }
    }
}

@Component
@ConfigurationProperties(prefix = "cardra.research.external")
class ExternalResearchConfig {
    var enabled: Boolean = true
    var endpoint: String = ""
    var timeoutSeconds: Long = 6
}

@Component("externalResearchDataAdapter")
class ExternalResearchDataAdapter(
    private val config: ExternalResearchConfig,
    restTemplateBuilder: RestTemplateBuilder,
) : ResearchDataAdapter {
    private val restTemplate: RestTemplate =
        restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .readTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .build()

    override fun fetch(
        req: ResearchRunRequest,
        traceId: String,
    ): ResearchDataPayload {
        if (!config.enabled) {
            throw ExternalResearchSchemaError("External research adapter is disabled")
        }
        if (config.endpoint.isBlank()) {
            throw ExternalResearchSchemaError("External research endpoint is required when enabled")
        }

        val request =
            RequestEntity
                .post(URI(config.endpoint))
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    ExternalResearchRequest(
                        keyword = req.keyword,
                        language = req.language,
                        country = req.country,
                        timeRange = req.timeRange,
                        maxItems = req.maxItems,
                        summaryLevel = req.summaryLevel,
                        factcheckMode = req.factcheckMode,
                        traceId = traceId,
                    ),
                )

        val response =
            try {
                restTemplate.exchange(request, ExternalResearchResponse::class.java)
            } catch (e: RestClientResponseException) {
                throw mapHttpError(e)
            } catch (_: Exception) {
                throw ExternalResearchTimeoutError("External research call failed")
            }

        if (!response.statusCode.is2xxSuccessful) {
            throw ExternalResearchUpstreamError("External research returned non-success status: ${response.statusCode}")
        }

        val body = response.body ?: throw ExternalResearchSchemaError("External research returned empty body")
        if (body.items.isEmpty()) {
            throw ExternalResearchSchemaError("External research returned no items")
        }
        if (body.summary == null) {
            throw ExternalResearchSchemaError("External research returned no summary")
        }

        return ResearchDataPayload(
            items = body.items,
            summary = body.summary,
            providerCalls = 1,
            cacheHit = body.cacheHit ?: false,
        )
    }

    private fun mapHttpError(e: RestClientResponseException): ExternalResearchError {
        val status = e.statusCode.value()
        return when {
            status == 429 -> {
                ExternalResearchRateLimitError("External research call failed: ${e.statusCode} ${e.statusText}")
            }

            status in 500..599 -> {
                ExternalResearchUpstreamError("External research call failed: ${e.statusCode} ${e.statusText}")
            }

            else -> {
                ExternalResearchSchemaError("External research rejected request: ${e.statusCode} ${e.statusText}")
            }
        }
    }
}

@Component("primaryResearchDataAdapter")
class PrimaryResearchDataAdapter(
    @Qualifier("openAiResearchDataAdapter")
    private val openAi: ResearchDataAdapter,
    @Qualifier("externalResearchDataAdapter")
    private val external: ResearchDataAdapter,
) : ResearchDataAdapter {
    override fun fetch(
        req: ResearchRunRequest,
        traceId: String,
    ): ResearchDataPayload {
        val errors = mutableListOf<ExternalResearchError>()
        try {
            return openAi.fetch(req, traceId)
        } catch (e: ExternalResearchError) {
            errors.add(e)
        }

        try {
            return external.fetch(req, traceId)
        } catch (e: ExternalResearchError) {
            errors.add(e)
        }

        throw errors.firstOrNull() ?: ExternalResearchSchemaError("No primary research adapter is available")
    }
}

@Component("stubResearchDataAdapter")
class StubResearchDataAdapter : ResearchDataAdapter {
    override fun fetch(
        req: ResearchRunRequest,
        traceId: String,
    ): ResearchDataPayload {
        val now = Instant.now().toString()
        return ResearchDataPayload(
            items = listOf(buildStubItem(req, now)),
            summary =
                ResearchSummaryDto(
                    brief = "요약: ${req.keyword} 이슈는 모니터링 단계입니다.",
                    analystNote = "근거 수집량이 적어 추가 확인이 필요합니다.",
                    riskFlags = listOf("insufficient_evidence"),
                ),
            providerCalls = 1,
            cacheHit = false,
        )
    }
}

@Component("fallbackResearchDataAdapter")
class FallbackResearchDataAdapter(
    @Qualifier("primaryResearchDataAdapter")
    private val primary: ResearchDataAdapter,
    @Qualifier("stubResearchDataAdapter")
    private val fallback: ResearchDataAdapter,
    private val fallbackConfig: ResearchFallbackConfig,
) : ResearchDataAdapter {
    private val logger = LoggerFactory.getLogger(FallbackResearchDataAdapter::class.java)

    override fun fetch(
        req: ResearchRunRequest,
        traceId: String,
    ): ResearchDataPayload =
        try {
            primary.fetch(req, traceId)
        } catch (e: ExternalResearchError) {
            if (!fallbackConfig.allowStubFallback) {
                logger.warn("research_fallback_disabled: keyword={} reason={}", req.keyword, e::class.simpleName)
                throw e
            }
            logger.warn("research_fallback_used: keyword={} reason={}", req.keyword, e::class.simpleName)
            fallback.fetch(req, traceId)
        } catch (e: Exception) {
            if (!fallbackConfig.allowStubFallback) {
                logger.warn("research_fallback_disabled: keyword={} reason=UNKNOWN", req.keyword)
                throw ExternalResearchUpstreamError("Research fetch failed unexpectedly")
            }
            logger.warn("research_fallback_used: keyword={} reason={}", req.keyword, "UNKNOWN")
            fallback.fetch(req, traceId)
        }
}

private const val OPENAI_RESEARCH_SYSTEM_PROMPT =
    "You are a research extraction engine. Return JSON only with camelCase keys and no markdown."

private fun buildOpenAiUserPrompt(
    req: ResearchRunRequest,
    traceId: String,
): String =
    """
    Build research payload for the request below.
    Request:
    - keyword: ${req.keyword}
    - language: ${req.language}
    - country: ${req.country}
    - timeRange: ${req.timeRange}
    - maxItems: ${req.maxItems}
    - summaryLevel: ${req.summaryLevel}
    - factcheckMode: ${req.factcheckMode}
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
    - Always include all required keys
    - Do not include markdown fences
    """.trimIndent()

private fun stripCodeFence(content: String): String {
    val trimmed = content.trim()
    if (!trimmed.startsWith("```")) {
        return trimmed
    }
    return trimmed
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
}

private fun buildStubItem(
    req: ResearchRunRequest,
    now: String,
): ResearchItemDto =
    ResearchItemDto(
        itemId =
            java.util.UUID
                .randomUUID()
                .toString(),
        title = "${req.keyword}: 최근 동향 확인",
        snippet = "최근 ${req.keyword}와 관련된 주요 변화는 출처 기반 수집으로 검증 경로를 함께 점검 중입니다.",
        source =
            com.cardra.server.dto.ResearchSourceDto(
                publisher = "trend-feed",
                url = "https://example.com/search?keyword=${req.keyword}",
                sourceType = "official",
            ),
        timestamps =
            com.cardra.server.dto.ResearchTimestampsDto(
                publishedAt = now,
                collectedAt = now,
                lastVerifiedAt = now,
            ),
        factcheck =
            com.cardra.server.dto.ResearchFactcheckDto(
                status = "insufficient",
                confidence = 0.58,
                confidenceReasons = listOf("initial_fetch", "single_source"),
                claims =
                    listOf(
                        com.cardra.server.dto.ResearchClaimDto(
                            claimText = "${req.keyword} 관련 보도 데이터가 제한적입니다.",
                            verdict = "insufficient",
                            evidenceIds = listOf("ev-1", "ev-2"),
                        ),
                    ),
            ),
        trend =
            com.cardra.server.dto.ResearchTrendDto(
                trendScore = 76,
                velocity = 1.3,
                regionRank = 4,
            ),
    )

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExternalResearchRequest(
    val keyword: String,
    val language: String,
    val country: String,
    val timeRange: String,
    val maxItems: Int,
    val summaryLevel: String,
    val factcheckMode: String,
    val traceId: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExternalResearchResponse(
    val items: List<ResearchItemDto> = emptyList(),
    val summary: ResearchSummaryDto? = null,
    val cacheHit: Boolean? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChatResponse(
    val choices: List<OpenAiChoice> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChoice(
    val message: OpenAiMessage? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiMessage(
    val content: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiResearchPayload(
    val items: List<ResearchItemDto> = emptyList(),
    val summary: ResearchSummaryDto? = null,
)

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
