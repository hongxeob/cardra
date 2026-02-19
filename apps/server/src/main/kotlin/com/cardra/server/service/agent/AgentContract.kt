package com.cardra.server.service.agent

import java.time.Instant

enum class AgentStatus {
    OK,
    PARTIAL,
    ERROR,
    FALLBACK,
}

enum class AgentErrorCode {
    TIMEOUT,
    RATE_LIMIT,
    INVALID_SCHEMA,
    UPSTREAM_5XX,
    UNKNOWN,
}

enum class FallbackReason {
    NONE,
    PRIMARY_FAILED,
    SCHEMA_INVALID,
    TIMEOUT,
    RATE_LIMIT,
    UPSTREAM_ERROR,
    UNKNOWN,
}

data class AgentRequest(
    val requestId: String,
    val traceId: String,
    val agentName: String = "writer",
    val agentVersion: String = "v1",
    val taskType: String,
    val input: Map<String, String>,
    val timeoutMs: Long = 8000,
    val locale: String = "ko-KR",
)

data class AgentResponse(
    val requestId: String,
    val traceId: String,
    val status: AgentStatus,
    val output: Map<String, Any>?,
    val errorCode: AgentErrorCode? = null,
    val errorMessage: String? = null,
    val fallbackUsed: Boolean = false,
    val fallbackReason: FallbackReason = FallbackReason.NONE,
    val provider: String = "local",
    val latencyMs: Long = 0,
    val respondedAt: Instant = Instant.now(),
)
