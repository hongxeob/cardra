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
                "deep" -> composeResearchBackedCards(keyword, req.tone, req.category)
                else -> agentAdapter.composeCards(keyword, req.tone, req.category)
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
        category: String,
    ): List<CardItem> {
        val research =
            researchService.runResearch(
                ResearchRunRequest(
                    keyword = keyword,
                    maxItems = 5,
                    summaryLevel = "standard",
                    factcheckMode = "strict",
                    tone = tone,
                    category = category,
                ),
            )
        val uniqueSources = research.items.map { it.source.url }.filter { it.isNotBlank() }.distinct()
        val sourceAt = research.generatedAt

        val card1Body = fitCardBody(research.summary.brief)
        val card2Body = fitCardBody(research.items.firstOrNull()?.snippet ?: research.summary.brief)
        val card3Body =
            fitCardBody(
                research.summary.analystNote.ifBlank {
                    research.summary.riskFlags.firstOrNull().orEmpty().ifBlank { research.summary.brief }
                },
            )
        val card1Title =
            pickCardTitle(
                candidates = listOf(research.items.getOrNull(0)?.title, research.summary.brief),
                fallback = "$keyword 딥 리서치 요약",
            )
        val card2Title =
            pickCardTitle(
                candidates =
                    listOf(
                        research.items.getOrNull(1)?.title,
                        research.items.getOrNull(0)?.title,
                        research.items.firstOrNull()?.snippet,
                    ),
                fallback = "$keyword 팩트체크 포인트",
            )
        val card3Title =
            pickCardTitle(
                candidates = listOf(research.summary.analystNote, research.summary.riskFlags.firstOrNull()),
                fallback = "$keyword 리스크와 액션",
            )

        return listOf(
            CardItem(
                title = card1Title,
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
                title = card2Title,
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
                title = card3Title,
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

    private fun pickCardTitle(
        candidates: List<String?>,
        fallback: String,
        max: Int = 40,
    ): String {
        val selected = candidates.asSequence().mapNotNull { normalizeTitleCandidate(it) }.firstOrNull() ?: fallback
        return if (selected.length > max) selected.take(max - 3).trimEnd() + "..." else selected
    }

    private fun normalizeTitleCandidate(raw: String?): String? {
        if (raw.isNullOrBlank()) {
            return null
        }
        val compact = raw.replace(Regex("\\s+"), " ").trim()
        if (compact.isBlank()) {
            return null
        }
        val firstLine = compact.substringBefore('\n').trim()
        val firstSentence = firstLine.substringBefore('.').substringBefore('!').substringBefore('?').trim()
        return firstSentence.ifBlank { null }
    }

    private fun fitCardBody(
        raw: String,
        max: Int = 220,
    ): String {
        val body = raw.replace(Regex("\\s+"), " ").trim()
        return if (body.length > max) body.take(max - 3).trimEnd() + "..." else body
    }
}
