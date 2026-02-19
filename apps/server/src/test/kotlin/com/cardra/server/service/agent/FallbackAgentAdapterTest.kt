package com.cardra.server.service.agent

import com.cardra.server.dto.CardItem
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FallbackAgentAdapterTest {
    @Test
    fun `fallback should return safety cards on delegate exception`() {
        val primary = mockk<AgentAdapter>()
        val fallback = mockk<AgentAdapter>()

        every { primary.composeCards("AI") } throws RuntimeException("agent down")
        every {
            fallback.composeCards("AI")
        } returns
            listOf(
                CardItem(
                    title = "fallback",
                    body = "safe",
                    source = listOf("agent://fallback"),
                    sourceAt = "2026-02-19T00:00:00Z",
                ),
            )

        val adapter = FallbackAgentAdapter(primary, fallback)
        val cards = adapter.composeCards("AI")

        assertEquals(1, cards.size)
        assertEquals("fallback", cards[0].title)
        assertTrue(cards[0].source.contains("agent://fallback"))
    }
}
