package com.cardra.server.service.agent

import com.cardra.server.dto.CardItem
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.Duration
import java.time.Instant

sealed class ExternalAgentError(message: String, val reason: ExternalAgentErrorReason) : RuntimeException(message)

class ExternalAgentTimeoutError(message: String) : ExternalAgentError(message, ExternalAgentErrorReason.TIMEOUT)

class ExternalAgentRateLimitError(message: String) : ExternalAgentError(message, ExternalAgentErrorReason.RATE_LIMIT)

class ExternalAgentUpstreamError(message: String, reason: ExternalAgentErrorReason) :
    ExternalAgentError(message, reason)

class ExternalAgentSchemaError(message: String) : ExternalAgentError(message, ExternalAgentErrorReason.INVALID_SCHEMA)

enum class ExternalAgentErrorReason {
    TIMEOUT,
    RATE_LIMIT,
    INVALID_SCHEMA,
    UPSTREAM_5XX,
    UNKNOWN,
}

@Component("externalAgentAdapter")
class ExternalAgentAdapter(
    private val config: ExternalAgentConfig,
    private val restTemplateBuilder: RestTemplateBuilder,
) : AgentAdapter {
    private val restTemplate: RestTemplate =
        restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .readTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .build()

    override fun composeCards(
        keyword: String,
        tone: String,
        category: String,
    ): List<CardItem> {
        if (!config.enabled) {
            throw ExternalAgentSchemaError("External agent is disabled")
        }
        if (config.endpoint.isBlank()) {
            throw ExternalAgentSchemaError("External agent endpoint is required when enabled")
        }

        val request =
            RequestEntity
                .post(URI(config.endpoint))
                .contentType(MediaType.APPLICATION_JSON)
                .body(ExternalComposeRequest(keyword = keyword, tone = tone, category = category))

        val response =
            try {
                restTemplate.exchange(request, ExternalComposeResponse::class.java)
            } catch (e: RestClientResponseException) {
                throw mapHttpError(e)
            } catch (_: Exception) {
                throw ExternalAgentTimeoutError("External agent call failed")
            }

        require(response.statusCode.is2xxSuccessful) {
            "External agent returned non-success status: ${response.statusCode}"
        }

        val body = response.body ?: throw ExternalAgentSchemaError("External agent returned empty body")
        val cards = body.cards
        require(cards.isNotEmpty()) { "External agent returned no cards" }

        return cards.map {
            if (it.title.isBlank() || it.body.isBlank() || it.source.isEmpty()) {
                throw ExternalAgentSchemaError("External agent returned invalid card structure")
            }

            CardItem(
                title = it.title,
                body = it.body,
                source = it.source,
                sourceAt = it.sourceAt ?: Instant.now().toString(),
            )
        }
    }

    private fun mapHttpError(e: RestClientResponseException): ExternalAgentError {
        val status = e.statusCode.value()
        return when {
            status == 429 ->
                ExternalAgentRateLimitError("External agent call failed: ${e.statusCode} ${e.statusText}")
            status in 500..599 ->
                ExternalAgentUpstreamError(
                    "External agent call failed: ${e.statusCode} ${e.statusText}",
                    ExternalAgentErrorReason.UPSTREAM_5XX,
                )
            else ->
                ExternalAgentUpstreamError(
                    "External agent call failed: ${e.statusCode} ${e.statusText}",
                    ExternalAgentErrorReason.UNKNOWN,
                )
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExternalComposeRequest(
    val keyword: String,
    val tone: String = "neutral",
    val category: String = "",
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExternalComposeResponse(
    val cards: List<ExternalComposeCard>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExternalComposeCard(
    val title: String,
    val body: String,
    val source: List<String> = emptyList(),
    val sourceAt: String? = null,
)
