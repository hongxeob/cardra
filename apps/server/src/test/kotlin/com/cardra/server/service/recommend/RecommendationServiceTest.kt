package com.cardra.server.service.recommend

import com.cardra.server.dto.RecommendEvent
import com.cardra.server.dto.RecommendEventRequest
import com.cardra.server.dto.RecommendKeywordRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class RecommendationServiceTest {
    private val service = RecommendationService()

    @Test
    fun `recommend uses personalized strategy after event history is ingested`() {
        service.ingestEvents(
            RecommendEventRequest(
                userId = "user-personalized",
                events =
                    listOf(
                        RecommendEvent(
                            eventType = "search",
                            keyword = "AI",
                            eventTs = "2026-02-19T00:00:00Z",
                        ),
                    ),
            ),
        )

        val response =
            service.recommend(
                RecommendKeywordRequest(
                    userId = "user-personalized",
                    currentQuery = "트렌드",
                    categoryId = "tech",
                    limit = 3,
                ),
            )

        assertEquals("user-personalized", response.userId)
        assertEquals("PERSONALIZED", response.strategy)
        assertFalse(response.fallbackUsed)
        assertEquals("vector_personalized", response.candidates[0].source)
        assertEquals(3, response.candidates.size)
    }

    @Test
    fun `recommend falls back to session context when no history but query exists`() {
        val response =
            service.recommend(
                RecommendKeywordRequest(
                    userId = "user-session",
                    currentQuery = "AI",
                    categoryId = "tech",
                    limit = 2,
                ),
            )

        assertEquals("SESSION_CONTEXT", response.strategy)
        assertEquals(true, response.fallbackUsed)
        assertEquals("session_context", response.candidates[0].reasons[0])
    }

    @Test
    fun `recommend falls back to global popular when no context`() {
        val response =
            service.recommend(
                RecommendKeywordRequest(
                    userId = "user-global",
                    currentQuery = null,
                    categoryId = "lifestyle",
                    limit = 2,
                ),
            )

        assertEquals("GLOBAL_POPULAR", response.strategy)
        assertEquals("lifestyle 인사이트 #1", response.candidates[0].keyword)
    }
}
