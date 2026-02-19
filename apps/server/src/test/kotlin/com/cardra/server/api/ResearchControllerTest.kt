package com.cardra.server.api

import com.cardra.server.dto.ResearchJobCancelResponse
import com.cardra.server.dto.ResearchJobCreateRequest
import com.cardra.server.dto.ResearchJobCreateResponse
import com.cardra.server.dto.ResearchJobErrorResponse
import com.cardra.server.dto.ResearchJobResultResponse
import com.cardra.server.dto.ResearchJobStatusResponse
import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchRunResponse
import com.cardra.server.exception.ResearchJobNotFoundException
import com.cardra.server.service.research.ResearchJobService
import com.cardra.server.service.research.ResearchService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ResearchControllerTest {
    private val service: ResearchService = mockk(relaxed = true)
    private val jobService: ResearchJobService = mockk(relaxed = true)
    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(ResearchController(service, jobService))
            .setControllerAdvice(com.cardra.server.exception.GlobalExceptionHandler())
            .build()

    private val mapper = ObjectMapper()

    @Test
    fun `run research returns data`() {
        val response =
            com.cardra.server.dto.ResearchRunResponse(
                traceId = "trc-1",
                status = "completed",
                generatedAt = "2026-02-19T00:00:00Z",
                query =
                    com.cardra.server.dto.ResearchQuery(
                        keyword = "AI",
                        language = "ko",
                        country = "KR",
                        timeRange = "24h",
                    ),
                items =
                    listOf(
                        com.cardra.server.dto.ResearchItemDto(
                            itemId = "item-1",
                            title = "AI",
                            snippet = "요약",
                            source =
                                com.cardra.server.dto.ResearchSourceDto(
                                    publisher = "news",
                                    url = "https://example.com",
                                    sourceType = "news",
                                ),
                            timestamps =
                                com.cardra.server.dto.ResearchTimestampsDto(
                                    publishedAt = "2026-02-19T00:00:00Z",
                                    collectedAt = "2026-02-19T00:00:00Z",
                                    lastVerifiedAt = "2026-02-19T00:00:00Z",
                                ),
                            factcheck =
                                com.cardra.server.dto.ResearchFactcheckDto(
                                    status = "insufficient",
                                    confidence = 0.5,
                                    confidenceReasons = listOf("initial"),
                                    claims =
                                        listOf(
                                            com.cardra.server.dto.ResearchClaimDto(
                                                claimText = "AI가 관련 이슈",
                                                verdict = "insufficient",
                                                evidenceIds = listOf("ev-1"),
                                            ),
                                        ),
                                ),
                            trend =
                                com.cardra.server.dto.ResearchTrendDto(
                                    trendScore = 75,
                                    velocity = 1.0,
                                    regionRank = 2,
                                ),
                        ),
                    ),
                summary =
                    com.cardra.server.dto.ResearchSummaryDto(
                        brief = "brief",
                        analystNote = "note",
                        riskFlags = emptyList(),
                    ),
            )

        every { service.runResearch(any(), any()) } returns response

        val req =
            ResearchRunRequest(
                keyword = "AI",
                language = "ko",
                country = "KR",
                timeRange = "24h",
                maxItems = 5,
                summaryLevel = "standard",
                factcheckMode = "strict",
            )

        mvc.perform(
            post("/api/v1/research/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("completed"))
            .andExpect(jsonPath("$.items.length()").value(1))
    }

    @Test
    fun `create research job`() {
        every { jobService.createJob(any()) } returns
            ResearchJobCreateResponse(
                jobId = "job-1",
                status = "queued",
                traceId = "trc-1",
            )

        mvc.perform(
            post("/api/v1/research/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        ResearchJobCreateRequest(
                            keyword = "AI",
                            language = "ko",
                            country = "KR",
                        ),
                    ),
                ),
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.jobId").value("job-1"))
    }

    @Test
    fun `get research job status`() {
        every { jobService.getStatus("job-1") } returns
            ResearchJobStatusResponse(
                jobId = "job-1",
                status = "running",
                createdAt = "2026-02-19T00:00:00Z",
                updatedAt = "2026-02-19T00:00:00Z",
            )

        mvc.perform(get("/api/v1/research/jobs/job-1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("running"))
            .andExpect(jsonPath("$.jobId").value("job-1"))
    }

    @Test
    fun `get research job result`() {
        every { jobService.getResult("job-1") } returns
            ResearchJobResultResponse(jobId = "job-1", status = "completed")

        mvc.perform(get("/api/v1/research/jobs/job-1/result"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("completed"))
            .andExpect(jsonPath("$.jobId").value("job-1"))
    }

    @Test
    fun `get research job result with retryable error`() {
        every { jobService.getResult("job-1") } returns
            ResearchJobResultResponse(
                jobId = "job-1",
                status = "failed",
                error =
                    ResearchJobErrorResponse(
                        code = "RESEARCH_JOB_FAILED",
                        message = "provider timeout",
                        retryable = true,
                        retryAfter = 5,
                        traceId = "trc-1",
                        usage = null,
                    ),
            )

        mvc.perform(get("/api/v1/research/jobs/job-1/result"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("failed"))
            .andExpect(jsonPath("$.error.code").value("RESEARCH_JOB_FAILED"))
            .andExpect(jsonPath("$.error.retryable").value(true))
            .andExpect(jsonPath("$.error.retryAfter").value(5))
    }

    @Test
    fun `cancel research job`() {
        every { jobService.cancelJob("job-1") } returns
            ResearchJobCancelResponse(
                jobId = "job-1",
                status = "cancelled",
                cancelled = true,
            )

        mvc.perform(post("/api/v1/research/jobs/job-1/cancel"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.cancelled").value(true))
    }

    @Test
    fun `get research job status not found`() {
        every { jobService.getStatus("missing") } throws ResearchJobNotFoundException("missing")

        mvc.perform(get("/api/v1/research/jobs/missing"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("RESEARCH_JOB_NOT_FOUND"))
    }

    @Test
    fun `create job with same idempotency key returns same job id`() {
        every { jobService.createJob(any()) } returnsMany
            listOf(
                ResearchJobCreateResponse(
                    jobId = "job-1",
                    status = "queued",
                    traceId = "trc-1",
                ),
                ResearchJobCreateResponse(
                    jobId = "job-1",
                    status = "queued",
                    traceId = "trc-1",
                ),
            )

        val req =
            ResearchJobCreateRequest(
                keyword = "AI",
                language = "ko",
                country = "KR",
                idempotencyKey = "idem-1",
            )

        val body = mapper.writeValueAsString(req)

        mvc.perform(
            post("/api/v1/research/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.jobId").value("job-1"))

        mvc.perform(
            post("/api/v1/research/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.jobId").value("job-1"))
    }
}
