package com.cardra.server.service.agent

import com.cardra.server.dto.CardItem
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class FallbackAgentAdapter(
    @Qualifier("externalAgentAdapter")
    private val primaryAgent: AgentAdapter,
    @Qualifier("mockAgentAdapter")
    private val fallbackAgent: AgentAdapter,
) : AgentAdapter {
    private val logger = LoggerFactory.getLogger(FallbackAgentAdapter::class.java)

    override fun composeCards(keyword: String): List<CardItem> {
        return try {
            primaryAgent.composeCards(keyword)
        } catch (e: ExternalAgentError) {
            val reason =
                when (e) {
                    is ExternalAgentRateLimitError -> "rate_limit"
                    is ExternalAgentTimeoutError -> "timeout"
                    is ExternalAgentUpstreamError -> "upstream_${e.reason.toString().lowercase()}"
                    is ExternalAgentSchemaError -> "schema"
                    else -> "unknown"
                }
            logger.warn("agent_fallback_used: reason={} keyword={}", reason, keyword)
            fallbackAgent.composeCards(keyword)
        } catch (_: Exception) {
            logger.warn("agent_fallback_used: reason=unknown keyword={}", keyword)
            fallbackAgent.composeCards(keyword)
        }
    }
}
