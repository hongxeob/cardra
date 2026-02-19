package com.cardra.server.service.agent

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FallbackAgentAdapterTest {
  @Test
  fun `fallback should return safety cards on delegate exception`() {
    val broken = mockk<MockAgentAdapter>()
    every { broken.composeCards("AI") } throws RuntimeException("agent down")

    val adapter = FallbackAgentAdapter(broken)
    val cards = adapter.composeCards("AI")

    assertEquals(3, cards.size)
    assertTrue(cards.all { it.source.contains("agent://fallback") })
    assertTrue(cards[0].body.contains("외부 에이전트"))
  }
}
