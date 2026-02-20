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
        require(items.all { it.body.length in 120..220 }) { "each card body should be between 120 and 220 chars" }
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
        val item1 = research.items.getOrNull(0)
        val item2 = research.items.getOrNull(1) ?: item1
        val uniqueSources = research.items.map { it.source.url }.filter { it.isNotBlank() }.distinct()
        val sourceAt = research.generatedAt
        val flags =
            if (research.summary.riskFlags.isNotEmpty()) {
                research.summary.riskFlags.joinToString(", ")
            } else {
                "low"
            }

        val card1Body =
            fitCardBody(
                """
                딥 리서치 기준 핵심 요약입니다. ${research.summary.brief}
                주요 근거는 ${item1?.source?.publisher ?: "research provider"}와
                ${item2?.source?.publisher ?: "secondary source"}이며, 요약은 실시간 수집 데이터와
                교차 검증 결과를 기준으로 작성했습니다.
                """.trimIndent(),
            )
        val card2Body =
            fitCardBody(
                """
                팩트체크 관점에서 가장 중요한 주장입니다.
                ${item1?.factcheck?.claims?.firstOrNull()?.claimText ?: "핵심 주장 데이터 확인 진행 중"}.
                판정은 ${item1?.factcheck?.claims?.firstOrNull()?.verdict ?: "insufficient"}이며
                confidence=${item1?.factcheck?.confidence ?: 0.0} 기준으로 해석해야 합니다.
                """.trimIndent(),
            )
        val card3Body =
            fitCardBody(
                """
                실행 판단 포인트입니다. analyst note: ${research.summary.analystNote}
                현재 risk flags는 $flags 이며, 후속 모니터링에서는 동일 지표를 같은 시간축으로 비교해
                과장 해석과 단기 노이즈를 분리해서 의사결정해야 합니다.
                """.trimIndent(),
            )

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
        min: Int = 120,
        max: Int = 220,
    ): String {
        val normalized = raw.replace(Regex("\\s+"), " ").trim()
        var body = normalized
        if (body.length > max) {
            body = body.take(max - 3).trimEnd() + "..."
        }
        if (body.length >= min) {
            return body
        }
        val filler = " 동일 기준의 추세와 근거 링크를 함께 비교해 판단 정확도를 유지하세요."
        while (body.length < min) {
            body = (body + filler).replace(Regex("\\s+"), " ").trim()
            if (body.length > max) {
                body = body.take(max - 3).trimEnd() + "..."
                break
            }
        }
        return body
    }
}
