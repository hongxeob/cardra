package com.cardra.server.service.research

import com.cardra.server.dto.ResearchClaimDto
import com.cardra.server.dto.ResearchFactcheckDto
import com.cardra.server.dto.ResearchItemDto
import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchSourceDto
import com.cardra.server.dto.ResearchSummaryDto
import com.cardra.server.dto.ResearchTimestampsDto
import com.cardra.server.dto.ResearchTrendDto
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResearchServiceTest {
    private val adapter: ResearchDataAdapter = mockk()
    private val service = ResearchService(adapter)

    @Test
    fun `runResearch delegates to adapter and preserves api contract fields`() {
        val req =
            ResearchRunRequest(
                keyword = "AI",
                language = "ko",
                country = "KR",
                timeRange = "24h",
            )
        val payload =
            ResearchDataPayload(
                items =
                    listOf(
                        ResearchItemDto(
                            itemId = "item-1",
                            title = "AI trend",
                            snippet = "요약",
                            source =
                                ResearchSourceDto(
                                    publisher = "news",
                                    url = "https://example.com",
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
                                    status = "insufficient",
                                    confidence = 0.6,
                                    confidenceReasons = listOf("single_source"),
                                    claims =
                                        listOf(
                                            ResearchClaimDto(
                                                claimText = "ai claim",
                                                verdict = "insufficient",
                                                evidenceIds = listOf("ev-1"),
                                            ),
                                        ),
                                ),
                            trend =
                                ResearchTrendDto(
                                    trendScore = 74,
                                    velocity = 1.2,
                                    regionRank = 2,
                                ),
                        ),
                    ),
                summary =
                    ResearchSummaryDto(
                        brief = "brief",
                        analystNote = "note",
                        riskFlags = listOf("insufficient_evidence"),
                    ),
                providerCalls = 2,
                cacheHit = true,
            )

        every { adapter.fetch(req, "trace-1") } returns payload

        val result = service.runResearch(req, "trace-1")

        assertEquals("trace-1", result.traceId)
        assertEquals("completed", result.status)
        assertEquals("AI", result.query.keyword)
        assertEquals(payload.items, result.items)
        assertEquals(payload.summary, result.summary)
        assertEquals(2, result.usage?.providerCalls)
        assertTrue((result.usage?.latencyMs ?: -1) >= 0)
        assertEquals(true, result.usage?.cacheHit)
    }
}
