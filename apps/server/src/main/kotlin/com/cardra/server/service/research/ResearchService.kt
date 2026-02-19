package com.cardra.server.service.research

import com.cardra.server.dto.ResearchQuery
import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchRunResponse
import com.cardra.server.dto.ResearchUsageDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class ResearchService(
    @Qualifier("fallbackResearchDataAdapter")
    private val adapter: ResearchDataAdapter,
) {
    fun runResearch(
        req: ResearchRunRequest,
        traceId: String = UUID.randomUUID().toString(),
    ): ResearchRunResponse {
        val startMs = System.currentTimeMillis()
        val payload = adapter.fetch(req, traceId)
        val now = Instant.now().toString()

        val latencyMs = System.currentTimeMillis() - startMs

        return ResearchRunResponse(
            traceId = traceId,
            status = "completed",
            generatedAt = now,
            query =
                ResearchQuery(
                    keyword = req.keyword,
                    language = req.language,
                    country = req.country,
                    timeRange = req.timeRange,
                ),
            items = payload.items,
            summary = payload.summary,
            usage =
                ResearchUsageDto(
                    providerCalls = payload.providerCalls,
                    latencyMs = latencyMs,
                    cacheHit = payload.cacheHit,
                ),
        )
    }
}
