package com.cardra.server.api

import com.cardra.server.domain.CardStatus
import com.cardra.server.dto.CardSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
@Tag(name = "System", description = "System health and readiness APIs")
class HealthController {
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Return server health summary")
    fun health(): CardSummaryResponse {
        return CardSummaryResponse(
            id = UUID.randomUUID(),
            keyword = "health",
            status = CardStatus.COMPLETED,
            createdAt = Instant.now(),
        )
    }
}
