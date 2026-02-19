package com.cardra.server.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class RecommendKeywordRequest(
    @field:NotBlank
    val userId: String,
    val currentQuery: String? = null,
    val locale: String? = null,
    val categoryId: String? = null,
    @field:Min(1)
    @field:Max(20)
    val limit: Int = 10,
    val excludeKeywords: List<String> = emptyList(),
    val debug: Boolean = false,
)

data class RecommendKeywordResponse(
    val requestId: String,
    val userId: String,
    val candidates: List<RecommendCandidate>,
    val fallbackUsed: Boolean,
    val fallbackReason: String,
    val strategy: String,
    val modelVersion: String,
    val latencyMs: Long,
)

enum class RecommendStrategy {
    PERSONALIZED,
    SESSION_CONTEXT,
    GLOBAL_POPULAR,
}

data class RecommendCandidate(
    val keyword: String,
    val score: Double,
    val reasons: List<String>,
    val source: String,
)

data class RecommendEventRequest(
    @field:NotBlank
    val userId: String,
    val sessionId: String? = null,
    @field:Valid
    @field:NotEmpty
    val events: List<RecommendEvent>,
)

data class RecommendEvent(
    @field:NotBlank
    val eventType: String,
    @field:NotBlank
    val keyword: String,
    @field:NotBlank
    val eventTs: String,
    val metadata: Map<String, String> = emptyMap(),
)

data class RecommendEventResponse(
    val accepted: Int,
    val failed: Int,
)
