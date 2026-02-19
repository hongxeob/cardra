package com.cardra.server.dto

import jakarta.validation.constraints.NotBlank

data class ResearchRunRequest(
    @field:NotBlank
    val keyword: String,
    val language: String = "ko",
    val country: String = "KR",
    val timeRange: String = "24h",
    val maxItems: Int = 5,
    val summaryLevel: String = "standard",
    val factcheckMode: String = "strict",
)

data class ResearchItemDto(
    val itemId: String,
    val title: String,
    val snippet: String,
    val source: ResearchSourceDto,
    val timestamps: ResearchTimestampsDto,
    val factcheck: ResearchFactcheckDto,
    val trend: ResearchTrendDto,
)

data class ResearchSourceDto(
    val publisher: String,
    val url: String,
    val sourceType: String,
    val author: String? = null,
)

data class ResearchTimestampsDto(
    val publishedAt: String,
    val collectedAt: String,
    val lastVerifiedAt: String,
)

data class ResearchFactcheckDto(
    val status: String,
    val confidence: Double,
    val confidenceReasons: List<String>,
    val claims: List<ResearchClaimDto>,
)

data class ResearchClaimDto(
    val claimText: String,
    val verdict: String,
    val evidenceIds: List<String>,
)

data class ResearchTrendDto(
    val trendScore: Int,
    val velocity: Double,
    val regionRank: Int,
)

data class ResearchSummaryDto(
    val brief: String,
    val analystNote: String,
    val riskFlags: List<String>,
)

data class ResearchRunResponse(
    val traceId: String,
    val status: String,
    val generatedAt: String,
    val query: ResearchQuery,
    val items: List<ResearchItemDto>,
    val summary: ResearchSummaryDto,
    val error: ResearchJobErrorResponse? = null,
    val usage: ResearchUsageDto? = null,
)

data class ResearchUsageDto(
    val providerCalls: Int,
    val latencyMs: Long,
    val cacheHit: Boolean,
)

data class ResearchQuery(
    val keyword: String,
    val language: String,
    val country: String,
    val timeRange: String,
)

data class ResearchJobCreateRequest(
    @field:NotBlank
    val keyword: String,
    val language: String = "ko",
    val country: String = "KR",
    val timeRange: String = "24h",
    val maxItems: Int = 5,
    val summaryLevel: String = "standard",
    val factcheckMode: String = "strict",
    val idempotencyKey: String? = null,
)

data class ResearchJobCreateResponse(
    val jobId: String,
    val status: String,
    val traceId: String,
)

data class ResearchJobStatusResponse(
    val jobId: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val error: ResearchJobErrorResponse? = null,
)

data class ResearchJobResultResponse(
    val jobId: String,
    val status: String,
    val result: ResearchRunResponse? = null,
    val error: ResearchJobErrorResponse? = null,
    val cache: ResearchJobCacheDto? = null,
)

data class ResearchJobCacheDto(
    val hit: Boolean,
    val ttlSec: Int,
)

data class ResearchJobErrorResponse(
    val code: String,
    val message: String,
    val retryable: Boolean,
    val retryAfter: Int? = null,
    val traceId: String? = null,
    val usage: ResearchUsageDto? = null,
)

data class ResearchJobCancelResponse(
    val jobId: String,
    val status: String,
    val cancelled: Boolean,
)
