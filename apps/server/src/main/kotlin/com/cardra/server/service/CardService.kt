package com.cardra.server.service

import com.cardra.server.domain.CardEntity
import com.cardra.server.domain.CardStatus
import com.cardra.server.dto.CardItem
import com.cardra.server.dto.CardResponse
import com.cardra.server.dto.CreateCardRequest
import com.cardra.server.exception.CardNotFoundException
import com.cardra.server.repository.CardRepository
import com.cardra.server.service.agent.AgentAdapter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class CardService(
    private val cardRepository: CardRepository,
    private val agentAdapter: AgentAdapter,
) {
    @Transactional
    fun createCard(req: CreateCardRequest): CardResponse {
        val keyword = req.keyword.trim()
        require(keyword.isNotBlank()) { "keyword must not be blank" }

        val items = agentAdapter.composeCards(keyword)
        validateItems(items)

        val entity =
            CardEntity(
                keyword = keyword,
                content = items.joinToString("\n---\n") { it.body },
                status = CardStatus.COMPLETED,
                sourceCount = items.sumOf { it.source.size },
            )
        val saved = cardRepository.save(entity)
        return CardResponse(
            id = saved.id ?: UUID.randomUUID(),
            keyword = saved.keyword,
            cards = items,
            status = saved.status,
            createdAt = saved.createdAt ?: Instant.now(),
        )
    }

    fun getCard(id: UUID): CardResponse {
        val e =
            cardRepository.findById(id).orElseThrow {
                CardNotFoundException("Card not found: $id")
            }
        val cards = parseCards(e.content)
        return CardResponse(e.id!!, e.keyword, cards, e.status, e.createdAt ?: Instant.now())
    }

    private fun parseCards(raw: String): List<CardItem> {
        val chunks = raw.split("\n---\n")
        return chunks.filter { it.isNotBlank() }.mapIndexed { idx, text ->
            CardItem(
                title = "카드 ${idx + 1}",
                body = text,
                source = listOf("agent://system", "agent://final"),
                sourceAt = Instant.now().toString(),
            )
        }
    }

    private fun validateItems(items: List<CardItem>) {
        require(items.isNotEmpty()) { "cards must not be empty" }
        require(items.size in 2..3) { "cards must be 2 or 3" }
        require(items.all { it.body.length in 120..220 }) { "each card body should be between 120 and 220 chars" }
    }
}
