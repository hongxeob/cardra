package com.cardra.server.api

import com.cardra.server.dto.RecommendEventRequest
import com.cardra.server.dto.RecommendEventResponse
import com.cardra.server.dto.RecommendKeywordRequest
import com.cardra.server.dto.RecommendKeywordResponse
import com.cardra.server.service.recommend.RecommendationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/recommend")
@Tag(name = "Recommend", description = "Recommendation and event ingestion APIs")
class RecommendController(
    private val recommendationService: RecommendationService,
) {
    @PostMapping("/keywords")
    @Operation(summary = "Recommend keywords", description = "Return recommended keywords for a user")
    fun recommend(
        @Valid @RequestBody req: RecommendKeywordRequest,
    ): RecommendKeywordResponse {
        return recommendationService.recommend(req)
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Ingest recommendation events", description = "Ingest user behavior events for recommendation tuning")
    fun events(
        @Valid @RequestBody req: RecommendEventRequest,
    ): RecommendEventResponse {
        return recommendationService.ingestEvents(req)
    }
}
