package com.cardra.server.service

import com.cardra.server.domain.CardEntity
import com.cardra.server.domain.CardStatus
import com.cardra.server.dto.CreateCardRequest
import com.cardra.server.exception.CardNotFoundException
import com.cardra.server.repository.CardRepository
import com.cardra.server.service.agent.AgentAdapter
import com.cardra.server.service.agent.MockAgentAdapter
import com.cardra.server.service.agent.NoopResearchProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional
import java.util.UUID

class CardServiceTest {
    private val repository: CardRepository = mockk(relaxed = true)
    private val adapter: AgentAdapter = MockAgentAdapter(NoopResearchProvider())
    private val service = CardService(repository, adapter)

    @Test
    fun `createCard should persist and return response`() {
        val saved = slot<CardEntity>()
        every { repository.save(capture(saved)) } answers { saved.captured }

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

    @Test
    fun `getCard should return parsed cards by id`() {
        val id = UUID.fromString("22222222-2222-2222-2222-222222222222")
        every { repository.findById(id) } returns
            Optional.of(
                CardEntity(
                    id = id,
                    keyword = "AI 에이전트",
                    content = "card body one\n---\ncard body two",
                    status = CardStatus.COMPLETED,
                    sourceCount = 2,
                    createdAt = Instant.parse("2026-02-19T00:00:00Z"),
                ),
            )

        val result = service.getCard(id)

        assertEquals("AI 에이전트", result.keyword)
        assertEquals(2, result.cards.size)
        assertEquals("카드 1", result.cards[0].title)
    }

    @Test
    fun `getCard should throw not found for missing id`() {
        val id = UUID.fromString("33333333-3333-3333-3333-333333333333")
        every { repository.findById(id) } returns Optional.empty()

        assertThrows(CardNotFoundException::class.java) {
            service.getCard(id)
        }
    }
}
