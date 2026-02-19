package com.cardra.server.api

import com.cardra.server.domain.CardStatus
import com.cardra.server.dto.CardItem
import com.cardra.server.dto.CardResponse
import com.cardra.server.dto.CreateCardRequest
import com.cardra.server.exception.CardNotFoundException
import com.cardra.server.exception.GlobalExceptionHandler
import com.cardra.server.service.CardService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.UUID

class CardControllerTest {
    private val service: CardService = mockk(relaxed = true)
    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(CardController(service))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    private val request = CreateCardRequest("AI", "neutral")

    @Test
    fun `generate returns accepted with payload`() {
        val response =
            CardResponse(
                id = UUID.randomUUID(),
                keyword = "AI",
                cards =
                    listOf(
                        CardItem(
                            title = "AI 지금 왜 중요한가?",
                            body = "AI 관련 이슈는 단기 반응보다 누적 신호를 같이 볼 때 판단 정확도가 높아집니다. 정책, 가격, 수요 신호를 함께 결합해 보아야 합니다.",
                            source = listOf("agent://research", "agent://analysis"),
                            sourceAt = "2026-02-19T00:00:00Z",
                        ),
                        CardItem(
                            title = "AI 체크 포인트",
                            body = "단기 유입과 장기 성장성의 분리점을 같이 기록하면 오판단을 줄일 수 있습니다. 가격 신호와 운영 지표를 함께 추적하면서 결정을 설계하면 품질이 올라갑니다.",
                            source = listOf("agent://analysis", "agent://editor"),
                            sourceAt = "2026-02-19T00:00:00Z",
                        ),
                        CardItem(
                            title = "AI 한줄 요약",
                            body = "최종 판단은 출처 2개 이상 교차 확인과 시점 정리가 핵심입니다. 동일 이슈라도 해석 편차를 비교해 리스크를 낮춰야 장기적으로 안정적인 카드 운영이 가능합니다.",
                            source = listOf("agent://validation", "agent://research"),
                            sourceAt = "2026-02-19T00:00:00Z",
                        ),
                    ),
                status = CardStatus.COMPLETED,
                createdAt = Instant.parse("2026-02-19T00:00:00Z"),
            )

        every { service.createCard(request) } returns response

        mvc.perform(
            post("/api/v1/cards/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"keyword\":\"AI\",\"tone\":\"neutral\"}"),
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.keyword").value("AI"))
            .andExpect(jsonPath("$.cards.length()").value(3))
    }

    @Test
    fun `get card returns ok payload`() {
        val cardId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val response =
            CardResponse(
                id = cardId,
                keyword = "AI",
                cards =
                    listOf(
                        CardItem(
                            title = "카드 1",
                            body = "테스트 카드 본문 예시 입니다. 실제 서비스에서는 여러 소스를 병합해 최신성 위주의 요약을 제공합니다.",
                            source = listOf("agent://system"),
                            sourceAt = "2026-02-19T00:00:00Z",
                        ),
                    ),
                status = CardStatus.COMPLETED,
                createdAt = Instant.parse("2026-02-19T00:00:00Z"),
            )

        every { service.getCard(cardId) } returns response

        mvc.perform(get("/api/v1/cards/$cardId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(cardId.toString()))
            .andExpect(jsonPath("$.cards.length()").value(1))
    }

    @Test
    fun `get card returns 404 if not found`() {
        val missingId = UUID.fromString("99999999-1111-1111-1111-111111111111")
        every { service.getCard(missingId) } throws CardNotFoundException("Card not found: $missingId")

        mvc.perform(get("/api/v1/cards/$missingId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("NOT_FOUND"))
            .andExpect(jsonPath("$.retryable").value(false))
    }

    @Test
    fun `health is okay`() {
        val mvc2: MockMvc =
            MockMvcBuilders.standaloneSetup(HealthController()).build()

        mvc2.perform(get("/api/v1/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.keyword").value("health"))
    }
}
