package com.cardra.server.service

import com.cardra.server.domain.CardEntity
import com.cardra.server.domain.CardStatus
import com.cardra.server.dto.CardItem
import com.cardra.server.dto.CardResponse
import com.cardra.server.dto.CardStyle
import com.cardra.server.dto.CreateCardRequest
import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.exception.CardNotFoundException
import com.cardra.server.repository.CardRepository
import com.cardra.server.service.agent.AgentAdapter
import com.cardra.server.service.research.ResearchService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class CardService(
    private val cardRepository: CardRepository,
    private val agentAdapter: AgentAdapter,
    private val researchService: ResearchService,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun createCard(req: CreateCardRequest): CardResponse {
        val keyword = req.keyword.trim()
        require(keyword.isNotBlank()) { "keyword must not be blank" }
        val mode = normalizeMode(req.mode)

        val items =
            when (mode) {
                "deep" -> composeResearchBackedCards(keyword, req.tone)
                else -> agentAdapter.composeCards(keyword)
            }
        validateItems(items)

        val entity =
            CardEntity(
                keyword = keyword,
                content = objectMapper.writeValueAsString(items),
                status = CardStatus.COMPLETED,
                sourceCount = items.sumOf { it.source.size },
            )
        val saved = cardRepository.save(entity)
        return CardResponse(
            id = saved.id ?: UUID.randomUUID(),
            keyword = saved.keyword,
            cards = items,
            status = saved.status,
            createdAt = saved.createdAt ?: Instant.now(),
        )
    }

    fun getCard(id: UUID): CardResponse {
        val e =
            cardRepository.findById(id).orElseThrow {
                CardNotFoundException("Card not found: $id")
            }
        val cards = parseCards(e.content)
        return CardResponse(e.id!!, e.keyword, cards, e.status, e.createdAt ?: Instant.now())
    }

    private fun parseCards(raw: String): List<CardItem> = objectMapper.readValue(raw)

    private fun validateItems(items: List<CardItem>) {
        require(items.isNotEmpty()) { "cards must not be empty" }
        require(items.size in 2..3) { "cards must be 2 or 3" }
        require(items.all { it.body.isNotBlank() }) { "each card body must not be blank" }
    }

    private fun normalizeMode(raw: String): String {
        val mode = raw.trim().lowercase().ifBlank { "quick" }
        require(mode == "quick" || mode == "deep") { "mode must be one of: quick, deep" }
        return mode
    }

    private fun composeResearchBackedCards(
        keyword: String,
        tone: String,
    ): List<CardItem> {
        val research =
            researchService.runResearch(
                ResearchRunRequest(
                    keyword = keyword,
                    maxItems = 5,
                    summaryLevel = "standard",
                    factcheckMode = "strict",
                ),
            )
        val uniqueSources = research.items.map { it.source.url }.filter { it.isNotBlank() }.distinct()
        val sourceAt = research.generatedAt

        val card1Body = fitCardBody(research.summary.brief)
        val card2Body = fitCardBody(research.items.firstOrNull()?.snippet ?: research.summary.brief)
        val card3Body = fitCardBody(research.summary.analystNote)

        return listOf(
            CardItem(
                title = "$keyword 딥 리서치 요약",
                body = card1Body,
                source = uniqueSources.ifEmpty { listOf("research://trace/${research.traceId}") },
                sourceAt = sourceAt,
                variant = "headline",
                style =
                    CardStyle(
                        tone = tone,
                        layout = "wide",
                        emphasis = "evidence",
                    ),
                tags = listOf("deep", "summary"),
                imageHint = "$keyword deep research summary",
            ),
            CardItem(
                title = "$keyword 팩트체크 포인트",
                body = card2Body,
                source = uniqueSources.take(2).ifEmpty { listOf("research://trace/${research.traceId}") },
                sourceAt = sourceAt,
                variant = "insight",
                style =
                    CardStyle(
                        tone = tone,
                        layout = "list",
                        emphasis = "factcheck",
                    ),
                tags = listOf("deep", "factcheck"),
                imageHint = "$keyword factcheck evidence",
            ),
            CardItem(
                title = "$keyword 리스크와 액션",
                body = card3Body,
                source = uniqueSources.takeLast(2).ifEmpty { listOf("research://trace/${research.traceId}") },
                sourceAt = sourceAt,
                variant = "summary",
                style =
                    CardStyle(
                        tone = tone,
                        layout = "compact",
                        emphasis = "decision",
                    ),
                tags = listOf("deep", "risk"),
                imageHint = "$keyword risk action",
            ),
        )
    }

    private fun fitCardBody(
        raw: String,
        max: Int = 220,
    ): String {
        val body = raw.replace(Regex("\\s+"), " ").trim()
        return if (body.length > max) body.take(max - 3).trimEnd() + "..." else body
    }
}
