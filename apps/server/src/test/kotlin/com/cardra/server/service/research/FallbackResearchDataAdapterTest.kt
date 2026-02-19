package com.cardra.server.service.research

import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchSummaryDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FallbackResearchDataAdapterTest {
    private val req = ResearchRunRequest(keyword = "AI")
    private val traceId = "trace-1"

    @Test
    fun `uses primary adapter on success`() {
        val primary: ResearchDataAdapter = mockk()
        val fallback: ResearchDataAdapter = mockk()
        val fallbackConfig = ResearchFallbackConfig().apply { allowStubFallback = true }
        val expected =
            ResearchDataPayload(
                items = emptyList(),
                summary =
                    ResearchSummaryDto(
                        brief = "ok",
                        analystNote = "ok",
                        riskFlags = emptyList(),
                    ),
            )
        every { primary.fetch(req, traceId) } returns expected

        val adapter = FallbackResearchDataAdapter(primary, fallback, fallbackConfig)
        val actual = adapter.fetch(req, traceId)

        assertEquals(expected, actual)
        verify(exactly = 0) { fallback.fetch(any(), any()) }
    }

    @Test
    fun `falls back when external adapter fails`() {
        val primary: ResearchDataAdapter = mockk()
        val fallback: ResearchDataAdapter = mockk()
        val fallbackConfig = ResearchFallbackConfig().apply { allowStubFallback = true }
        val expected =
            ResearchDataPayload(
                items = emptyList(),
                summary =
                    ResearchSummaryDto(
                        brief = "fallback",
                        analystNote = "fallback",
                        riskFlags = listOf("fallback"),
                    ),
            )
        every { primary.fetch(req, traceId) } throws ExternalResearchTimeoutError("timeout")
        every { fallback.fetch(req, traceId) } returns expected

        val adapter = FallbackResearchDataAdapter(primary, fallback, fallbackConfig)
        val actual = adapter.fetch(req, traceId)

        assertEquals(expected, actual)
        verify(exactly = 1) { fallback.fetch(req, traceId) }
    }

    @Test
    fun `throws primary error when fallback disabled`() {
        val primary: ResearchDataAdapter = mockk()
        val fallback: ResearchDataAdapter = mockk()
        val fallbackConfig = ResearchFallbackConfig().apply { allowStubFallback = false }
        every { primary.fetch(req, traceId) } throws ExternalResearchTimeoutError("timeout")

        val adapter = FallbackResearchDataAdapter(primary, fallback, fallbackConfig)

        assertThrows<ExternalResearchTimeoutError> {
            adapter.fetch(req, traceId)
        }
        verify(exactly = 0) { fallback.fetch(any(), any()) }
    }
}
