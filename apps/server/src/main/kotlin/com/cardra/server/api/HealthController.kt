package com.cardra.server.api

import com.cardra.server.domain.CardStatus
import com.cardra.server.dto.CardSummaryResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class HealthController {
  @GetMapping("/health")
  fun health(): CardSummaryResponse {
    return CardSummaryResponse(
      id = UUID.randomUUID(),
      keyword = "health",
      status = CardStatus.COMPLETED,
      createdAt = Instant.now(),
    )
  }
}
