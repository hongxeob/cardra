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
        val keyword = req.keyword.trim()
        require(keyword.isNotBlank()) { "keyword must not be blank" }

        val items = generateCards(keyword)
        validateItems(items)

        val entity = CardEntity(
            keyword = keyword,
            content = items.joinToString("\n---\n") { it.body },
            status = CardStatus.COMPLETED,
            sourceCount = items.sumOf { it.source.size }
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
                body = "$keyword 관련 핵심 이슈는 시장 구조 변화, 정책/조달 변수, 사용자 반응이 동시에 충돌할 때 가치를 가집니다. 지금 이 구간은 단발성 뉴스보다 연속성 있는 신호를 잡는 쪽이 정확도가 높습니다.",
                source = listOf("agent://research", "agent://analysis"),
                sourceAt = sourceTs
            ),
            CardItem(
                title = "$keyword, 체크할 포인트",
                body = "$keyword의 단기 리스크와 기회는 수요 신호 강도, 가격 안정성, 경쟁자의 대응 속도에 따라 빠르게 바뀝니다. 24~72시간 이슈만 보지 말고 주간 추세까지 같이 보며 판단 포인트를 나눠가세요.",
                source = listOf("agent://analysis", "agent://editor"),
                sourceAt = sourceTs
            ),
            CardItem(
                title = "$keyword 핵심 요약",
                body = "현재 기준의 신뢰도를 유지하려면 출처(최소 2개) 교차 확인을 루틴화하세요. 동일 이슈라도 시각이 다른 매체가 같은 지표를 어떻게 해석하는지 비교하면 오판위를 크게 낮출 수 있습니다.",
                source = listOf("agent://validation", "agent://research"),
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
                source = listOf("agent://system", "agent://final"),
                sourceAt = Instant.now().toString()
            )
        }
    }

    private fun validateItems(items: List<CardItem>) {
        require(items.isNotEmpty()) { "cards must not be empty" }
        require(items.size in 2..3) { "cards must be 2 or 3" }
        require(items.all { it.body.length in 40..220 }) { "each card body should be between 40 and 220 chars" }
    }
}
