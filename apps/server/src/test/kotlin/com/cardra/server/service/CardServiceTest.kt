package com.cardra.server.service

import com.cardra.server.domain.CardStatus
import com.cardra.server.domain.CardEntity
import com.cardra.server.dto.CreateCardRequest
import com.cardra.server.repository.CardRepository
import com.cardra.server.service.agent.AgentAdapter
import com.cardra.server.service.agent.MockAgentAdapter
import com.cardra.server.service.agent.NoopResearchProvider
import io.mockk.any
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CardServiceTest {
    private val repository: CardRepository = mockk(relaxed = true)
    private val adapter: AgentAdapter = MockAgentAdapter(NoopResearchProvider())
    private val service = CardService(repository, adapter)

    @Test
    fun `createCard should persist and return response`() {
        every { repository.save(any()) } answers {
            val entity = firstArg<CardEntity>()
            CardEntity(
                id = UUID.randomUUID(),
                keyword = entity.keyword,
                content = entity.content,
                status = entity.status,
                sourceCount = entity.sourceCount,
                createdAt = Instant.parse("2026-02-19T00:00:00Z")
            )
        }

        val result = service.createCard(CreateCardRequest(keyword = "AI 에이전트"))

        assertEquals("AI 에이전트", result.keyword)
        assertEquals(CardStatus.COMPLETED, result.status)
        assertEquals(3, result.cards.size)
    }

    @Test
    fun `createCard should reject blank keyword`() {
        assertThrows(IllegalArgumentException::class.java) {
            service.createCard(CreateCardRequest(keyword = " "))
        }
    }
}
