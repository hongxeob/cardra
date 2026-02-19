package com.cardra.server.api

import com.cardra.server.dto.RecommendEvent
import com.cardra.server.dto.RecommendEventRequest
import com.cardra.server.dto.RecommendKeywordRequest
import com.cardra.server.service.recommend.RecommendationService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class RecommendControllerTest {
    private val service: RecommendationService = mockk(relaxed = true)
    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(RecommendController(service))
            .setControllerAdvice(com.cardra.server.exception.GlobalExceptionHandler())
            .build()

    private val mapper = ObjectMapper()

    @Test
    fun `recommend keywords returns candidates`() {
        every { service.recommend(any()) } returns
            com.cardra.server.dto.RecommendKeywordResponse(
                requestId = "r1",
                userId = "u1",
                candidates =
                    listOf(
                        com.cardra.server.dto.RecommendCandidate(
                            keyword = "AI 브리핑",
                            score = 0.9,
                            reasons = listOf("semantic_similarity"),
                            source = "vector_personalized",
                        ),
                    ),
                fallbackUsed = false,
                fallbackReason = "PERSONALIZED",
                strategy = "PERSONALIZED",
                modelVersion = "emb-v1.0",
                latencyMs = 12,
            )

        val req =
            RecommendKeywordRequest(
                userId = "u1",
                currentQuery = "AI",
                locale = "ko-KR",
                categoryId = "tech",
                limit = 10,
            )

        mvc.perform(
            post("/api/v1/recommend/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId").value("u1"))
            .andExpect(jsonPath("$.candidates.length()").value(1))
    }

    @Test
    fun `recommend validation fail`() {
        val req =
            RecommendKeywordRequest(
                userId = "",
                currentQuery = "AI",
            )

        mvc.perform(
            post("/api/v1/recommend/keywords")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `events accepted`() {
        every { service.ingestEvents(any()) } returns com.cardra.server.dto.RecommendEventResponse(accepted = 1, failed = 0)

        val req =
            RecommendEventRequest(
                userId = "u1",
                sessionId = "s1",
                events =
                    listOf(
                        RecommendEvent(
                            eventType = "search",
                            keyword = "AI",
                            eventTs = "2026-02-19T00:00:00Z",
                            metadata = mapOf("device" to "ios"),
                        ),
                    ),
            )

        mvc.perform(
            post("/api/v1/recommend/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accepted").value(1))
    }
}
