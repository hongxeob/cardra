package com.cardra.server.service

import com.cardra.server.domain.CardEntity
import com.cardra.server.domain.CardStatus
import com.cardra.server.dto.CreateCardRequest
import com.cardra.server.dto.ResearchFactcheckDto
import com.cardra.server.dto.ResearchItemDto
import com.cardra.server.dto.ResearchQuery
import com.cardra.server.dto.ResearchRunResponse
import com.cardra.server.dto.ResearchSourceDto
import com.cardra.server.dto.ResearchSummaryDto
import com.cardra.server.dto.ResearchTimestampsDto
import com.cardra.server.dto.ResearchTrendDto
import com.cardra.server.dto.ResearchUsageDto
import com.cardra.server.exception.CardNotFoundException
import com.cardra.server.repository.CardRepository
import com.cardra.server.service.agent.AgentAdapter
import com.cardra.server.service.agent.MockAgentAdapter
import com.cardra.server.service.agent.NoopResearchProvider
import com.cardra.server.service.research.ResearchService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional
import java.util.UUID

class CardServiceTest {
    private val repository: CardRepository = mockk(relaxed = true)
    private val adapter: AgentAdapter = MockAgentAdapter(NoopResearchProvider())
    private val researchService: ResearchService = mockk(relaxed = true)
    private val service = CardService(repository, adapter, researchService)

    @Test
    fun `createCard should persist and return response`() {
        val saved = slot<CardEntity>()
        every { repository.save(capture(saved)) } answers { saved.captured }

        val result = service.createCard(CreateCardRequest(keyword = "AI 에이전트"))

        assertEquals("AI 에이전트", result.keyword)
        assertEquals(CardStatus.COMPLETED, result.status)
        assertEquals(3, result.cards.size)
        verify(exactly = 0) { researchService.runResearch(any(), any()) }
    }

    @Test
    fun `createCard deep mode should create research backed cards`() {
        val saved = slot<CardEntity>()
        every { repository.save(capture(saved)) } answers { saved.captured }
        every { researchService.runResearch(any(), any()) } returns
            ResearchRunResponse(
                traceId = "trace-1",
                status = "completed",
                generatedAt = "2026-02-19T00:00:00Z",
                query =
                    ResearchQuery(
                        keyword = "AI 에이전트",
                        language = "ko",
                        country = "KR",
                        timeRange = "24h",
                    ),
                items =
                    listOf(
                        ResearchItemDto(
                            itemId = "r1",
                            title = "AI 이슈",
                            snippet = "요약",
                            source =
                                ResearchSourceDto(
                                    publisher = "news",
                                    url = "https://example.com/1",
                                    sourceType = "news",
                                ),
                            timestamps =
                                ResearchTimestampsDto(
                                    publishedAt = "2026-02-19T00:00:00Z",
                                    collectedAt = "2026-02-19T00:00:00Z",
                                    lastVerifiedAt = "2026-02-19T00:00:00Z",
                                ),
                            factcheck =
                                ResearchFactcheckDto(
                                    status = "supported",
                                    confidence = 0.9,
                                    confidenceReasons = listOf("match"),
                                    claims = emptyList(),
                                ),
                            trend =
                                ResearchTrendDto(
                                    trendScore = 88,
                                    velocity = 1.2,
                                    regionRank = 1,
                                ),
                        ),
                        ResearchItemDto(
                            itemId = "r2",
                            title = "AI 확산",
                            snippet = "요약",
                            source =
                                ResearchSourceDto(
                                    publisher = "official",
                                    url = "https://example.com/2",
                                    sourceType = "official",
                                ),
                            timestamps =
                                ResearchTimestampsDto(
                                    publishedAt = "2026-02-19T00:00:00Z",
                                    collectedAt = "2026-02-19T00:00:00Z",
                                    lastVerifiedAt = "2026-02-19T00:00:00Z",
                                ),
                            factcheck =
                                ResearchFactcheckDto(
                                    status = "supported",
                                    confidence = 0.87,
                                    confidenceReasons = listOf("cross-check"),
                                    claims = emptyList(),
                                ),
                            trend =
                                ResearchTrendDto(
                                    trendScore = 81,
                                    velocity = 1.1,
                                    regionRank = 2,
                                ),
                        ),
                    ),
                summary =
                    ResearchSummaryDto(
                        brief = "딥리서치 핵심 요약",
                        analystNote = "근거 기반 분석",
                        riskFlags = listOf("volatility"),
                    ),
                usage =
                    ResearchUsageDto(
                        providerCalls = 1,
                        latencyMs = 1200,
                        cacheHit = false,
                    ),
            )

        val result = service.createCard(CreateCardRequest(keyword = "AI 에이전트", mode = "deep"))

        assertEquals(CardStatus.COMPLETED, result.status)
        assertEquals(3, result.cards.size)
        assertEquals("AI 에이전트 딥 리서치 요약", result.cards[0].title)
        verify(exactly = 1) { researchService.runResearch(any(), any()) }
    }

    @Test
    fun `createCard should reject blank keyword`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.createCard(CreateCardRequest(keyword = " "))
        }
    }

    @Test
    fun `getCard should return parsed cards by id`() {
        val id = UUID.fromString("22222222-2222-2222-2222-222222222222")
        every { repository.findById(id) } returns
            Optional.of(
                CardEntity(
                    id = id,
                    keyword = "AI 에이전트",
                    content = "card body one\n---\ncard body two",
                    status = CardStatus.COMPLETED,
                    sourceCount = 2,
                    createdAt = Instant.parse("2026-02-19T00:00:00Z"),
                ),
            )

        val result = service.getCard(id)

        assertEquals("AI 에이전트", result.keyword)
        assertEquals(2, result.cards.size)
        assertEquals("카드 1", result.cards[0].title)
    }

    @Test
    fun `getCard should throw not found for missing id`() {
        val id = UUID.fromString("33333333-3333-3333-3333-333333333333")
        every { repository.findById(id) } returns Optional.empty()

        assertThrows(CardNotFoundException::class.java) {
            service.getCard(id)
        }
    }
}
