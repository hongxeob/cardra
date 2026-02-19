package com.cardra.server.service.research

import com.cardra.server.domain.ResearchJobEntity
import com.cardra.server.domain.ResearchJobStatus
import com.cardra.server.dto.ResearchClaimDto
import com.cardra.server.dto.ResearchFactcheckDto
import com.cardra.server.dto.ResearchItemDto
import com.cardra.server.dto.ResearchJobCreateRequest
import com.cardra.server.dto.ResearchQuery
import com.cardra.server.dto.ResearchRunResponse
import com.cardra.server.dto.ResearchSourceDto
import com.cardra.server.dto.ResearchSummaryDto
import com.cardra.server.dto.ResearchTimestampsDto
import com.cardra.server.dto.ResearchTrendDto
import com.cardra.server.dto.ResearchUsageDto
import com.cardra.server.repository.ResearchJobRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional

class ResearchJobServiceTest {
    private val researchService: ResearchService = mockk(relaxed = true)
    private val repository: ResearchJobRepository = mockk(relaxed = true)
    private val mapper = jacksonObjectMapper()
    private val service = ResearchJobService(researchService, repository, mapper)

    @Test
    fun `createJob returns existing job when idempotency key exists`() {
        val existing =
            ResearchJobEntity(
                id = "job-1",
                keyword = "AI",
                requestKey = "AI|ko|KR|24h|5|standard|strict",
                traceId = "trace-1",
                status = ResearchJobStatus.RUNNING,
                createdAt = Instant.parse("2026-02-19T00:00:00Z"),
                updatedAt = Instant.parse("2026-02-19T00:00:00Z"),
            )
        every { repository.findFirstByIdempotencyKeyOrderByCreatedAtDesc("idem-1") } returns existing

        val response =
            service.createJob(
                ResearchJobCreateRequest(
                    keyword = "AI",
                    idempotencyKey = "idem-1",
                ),
            )

        assertEquals("job-1", response.jobId)
        assertEquals("running", response.status)
        assertEquals("trace-1", response.traceId)
    }

    @Test
    fun `createJob reuses completed result from persisted cache`() {
        val cachedResult = sampleResult(traceId = "trace-cached", cacheHit = false)
        val cachedEntity =
            ResearchJobEntity(
                id = "job-cached",
                keyword = "AI",
                requestKey = "AI|ko|KR|24h|5|standard|strict",
                traceId = "trace-cached",
                status = ResearchJobStatus.COMPLETED,
                resultJson = mapper.writeValueAsString(cachedResult),
                fromCache = false,
                createdAt = Instant.parse("2026-02-19T00:00:00Z"),
                updatedAt = Instant.parse("2026-02-19T00:00:00Z"),
            )

        every { repository.findFirstByRequestKeyAndStatusOrderByUpdatedAtDesc(any(), ResearchJobStatus.COMPLETED) } returns cachedEntity
        val saveSlot = slot<ResearchJobEntity>()
        every { repository.save(capture(saveSlot)) } answers { saveSlot.captured }

        val response = service.createJob(ResearchJobCreateRequest(keyword = "AI"))

        assertEquals("completed", response.status)
        assertEquals(saveSlot.captured.id, response.jobId)
        assertTrue(saveSlot.captured.fromCache)
        assertEquals(cachedEntity.resultJson, saveSlot.captured.resultJson)
    }

    @Test
    fun `cancelJob can cancel persisted queued job without active future`() {
        val queued =
            ResearchJobEntity(
                id = "job-1",
                keyword = "AI",
                requestKey = "AI|ko|KR|24h|5|standard|strict",
                traceId = "trace-1",
                status = ResearchJobStatus.QUEUED,
                createdAt = Instant.parse("2026-02-19T00:00:00Z"),
                updatedAt = Instant.parse("2026-02-19T00:00:00Z"),
            )
        every { repository.findById("job-1") } returns Optional.of(queued)
        val saveSlot = slot<ResearchJobEntity>()
        every { repository.save(capture(saveSlot)) } answers { saveSlot.captured }

        val response = service.cancelJob("job-1")

        assertTrue(response.cancelled)
        assertEquals("cancelled", response.status)
        assertEquals(ResearchJobStatus.CANCELLED, saveSlot.captured.status)
    }

    @Test
    fun `getResult exposes cache metadata for cached jobs`() {
        val cachedResult = sampleResult(traceId = "trace-1", cacheHit = false)
        val entity =
            ResearchJobEntity(
                id = "job-1",
                keyword = "AI",
                requestKey = "AI|ko|KR|24h|5|standard|strict",
                traceId = "trace-1",
                status = ResearchJobStatus.COMPLETED,
                resultJson = mapper.writeValueAsString(cachedResult),
                fromCache = true,
                createdAt = Instant.parse("2026-02-19T00:00:00Z"),
                updatedAt = Instant.parse("2026-02-19T00:00:00Z"),
            )
        every { repository.findById("job-1") } returns Optional.of(entity)

        val response = service.getResult("job-1")

        assertNotNull(response.cache)
        assertTrue(response.cache!!.hit)
        assertEquals(180, response.cache!!.ttlSec)
        assertNotNull(response.result)
        assertNotNull(response.result!!.usage)
        assertTrue(response.result!!.usage!!.cacheHit)
    }

    private fun sampleResult(
        traceId: String,
        cacheHit: Boolean,
    ): ResearchRunResponse {
        val now = "2026-02-19T00:00:00Z"
        return ResearchRunResponse(
            traceId = traceId,
            status = "completed",
            generatedAt = now,
            query =
                ResearchQuery(
                    keyword = "AI",
                    language = "ko",
                    country = "KR",
                    timeRange = "24h",
                ),
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
                                publishedAt = now,
                                collectedAt = now,
                                lastVerifiedAt = now,
                            ),
                        factcheck =
                            ResearchFactcheckDto(
                                status = "insufficient",
                                confidence = 0.5,
                                confidenceReasons = listOf("initial"),
                                claims =
                                    listOf(
                                        ResearchClaimDto(
                                            claimText = "AI topic",
                                            verdict = "insufficient",
                                            evidenceIds = listOf("ev-1"),
                                        ),
                                    ),
                            ),
                        trend =
                            ResearchTrendDto(
                                trendScore = 70,
                                velocity = 1.1,
                                regionRank = 3,
                            ),
                    ),
                ),
            summary =
                ResearchSummaryDto(
                    brief = "brief",
                    analystNote = "note",
                    riskFlags = emptyList(),
                ),
            usage =
                ResearchUsageDto(
                    providerCalls = 1,
                    latencyMs = 10,
                    cacheHit = cacheHit,
                ),
        )
    }
}
