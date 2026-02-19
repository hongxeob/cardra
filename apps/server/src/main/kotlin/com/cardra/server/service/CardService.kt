package com.cardra.server.service

import com.cardra.server.domain.CardEntity
import com.cardra.server.domain.CardStatus
import com.cardra.server.dto.CardItem
import com.cardra.server.dto.CardResponse
import com.cardra.server.dto.CreateCardRequest
import com.cardra.server.repository.CardRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class CardService(
    private val cardRepository: CardRepository
) {
    @Transactional
    fun createCard(req: CreateCardRequest): CardResponse {
        val items = generateCards(req.keyword)
        val entity = CardEntity(
            id = null,
            keyword = req.keyword,
            content = items.joinToString("\n---\n") { it.body },
            status = CardStatus.COMPLETED,
            sourceCount = items.flatMap { it.source }.size
        )
        val saved = cardRepository.save(entity)
        return CardResponse(
            id = saved.id ?: UUID.randomUUID(),
            keyword = saved.keyword,
            cards = items,
            status = saved.status,
            createdAt = saved.createdAt ?: Instant.now()
        )
    }

    fun getCard(id: UUID): CardResponse {
        val e = cardRepository.findById(id).orElseThrow { IllegalArgumentException("Card not found") }
        val cards = parseCards(e.content)
        return CardResponse(e.id!!, e.keyword, cards, e.status, e.createdAt ?: Instant.now())
    }

    private fun generateCards(keyword: String): List<CardItem> {
        val sourceTs = "2026-02-19T00:00:00Z"
        return listOf(
            CardItem(
                title = "$keyword 지금 왜 중요한가?",
                body = "입력 키워드 '$keyword'와 관련해 최근 대화/뉴스에서 반복 언급되는 핵심 포인트 1개를 요약해 제공합니다.",
                source = listOf("agent://research"),
                sourceAt = sourceTs
            ),
            CardItem(
                title = "$keyword, 체크할 포인트",
                body = "$keyword에 대한 추가 트렌드와 위험요인을 짧게 분해해 다음 의사결정에 필요한 시사점을 제시합니다.",
                source = listOf("agent://analysis"),
                sourceAt = sourceTs
            ),
            CardItem(
                title = "$keyword 핵심 요약",
                body = "$keyword는 단기·중장기 모두 정보 갱신 속도를 반영해 한 번 더 검증 후 공유하면 정확도가 높아집니다.",
                source = listOf("agent://editor"),
                sourceAt = sourceTs
            )
        )
    }

    private fun parseCards(raw: String): List<CardItem> {
        val chunks = raw.split("\n---\n")
        return chunks.filter { it.isNotBlank() }.mapIndexed { idx, text ->
            CardItem(
                title = "카드 ${idx + 1}",
                body = text,
                source = listOf("agent://system"),
                sourceAt = Instant.now().toString()
            )
        }
    }
}
