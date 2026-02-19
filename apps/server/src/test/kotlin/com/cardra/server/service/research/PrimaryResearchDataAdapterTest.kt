package com.cardra.server.service.research

import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchSummaryDto
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PrimaryResearchDataAdapterTest {
    private val req = ResearchRunRequest(keyword = "AI")
    private val traceId = "trace-1"

    @Test
    fun `uses openai first when available`() {
        val openAi: ResearchDataAdapter = mockk()
        val external: ResearchDataAdapter = mockk()
        val expected =
            ResearchDataPayload(
                items = emptyList(),
                summary =
                    ResearchSummaryDto(
                        brief = "openai",
                        analystNote = "openai",
                        riskFlags = emptyList(),
                    ),
            )
        every { openAi.fetch(req, traceId) } returns expected

        val adapter = PrimaryResearchDataAdapter(openAi, external)
        val actual = adapter.fetch(req, traceId)

        assertEquals(expected, actual)
        verify(exactly = 0) { external.fetch(any(), any()) }
    }

    @Test
    fun `falls through to external when openai fails`() {
        val openAi: ResearchDataAdapter = mockk()
        val external: ResearchDataAdapter = mockk()
        val expected =
            ResearchDataPayload(
                items = emptyList(),
                summary =
                    ResearchSummaryDto(
                        brief = "external",
                        analystNote = "external",
                        riskFlags = listOf("fallback"),
                    ),
            )
        every { openAi.fetch(req, traceId) } throws ExternalResearchSchemaError("disabled")
        every { external.fetch(req, traceId) } returns expected

        val adapter = PrimaryResearchDataAdapter(openAi, external)
        val actual = adapter.fetch(req, traceId)

        assertEquals(expected, actual)
        verify(exactly = 1) { external.fetch(req, traceId) }
    }

    @Test
    fun `throws when both primary adapters fail`() {
        val openAi: ResearchDataAdapter = mockk()
        val external: ResearchDataAdapter = mockk()

        every { openAi.fetch(req, traceId) } throws ExternalResearchSchemaError("disabled")
        every { external.fetch(req, traceId) } throws ExternalResearchUpstreamError("upstream")

        val adapter = PrimaryResearchDataAdapter(openAi, external)

        assertThrows(ExternalResearchSchemaError::class.java) {
            adapter.fetch(req, traceId)
        }
    }
}
