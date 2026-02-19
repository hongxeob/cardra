package com.cardra.server.service.research

import com.cardra.server.dto.ResearchClaimDto
import com.cardra.server.dto.ResearchFactcheckDto
import com.cardra.server.dto.ResearchItemDto
import com.cardra.server.dto.ResearchQuery
import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchRunResponse
import com.cardra.server.dto.ResearchSourceDto
import com.cardra.server.dto.ResearchSummaryDto
import com.cardra.server.dto.ResearchTimestampsDto
import com.cardra.server.dto.ResearchTrendDto
import com.cardra.server.dto.ResearchUsageDto
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class ResearchService {
    fun runResearch(
        req: ResearchRunRequest,
        traceId: String = UUID.randomUUID().toString(),
    ): ResearchRunResponse {
        val startMs = System.currentTimeMillis()
        val now = Instant.now().toString()

        val item =
            ResearchItemDto(
                itemId = UUID.randomUUID().toString(),
                title = "${req.keyword}: 최근 동향 확인",
                snippet = "최근 ${req.keyword}와 관련된 주요 변화는 출처 기반 수집으로 검증 경로를 함께 점검 중입니다.",
                source =
                    ResearchSourceDto(
                        publisher = "trend-feed",
                        url = "https://example.com/search?keyword=${req.keyword}",
                        sourceType = "official",
                    ),
                timestamps =
                    ResearchTimestampsDto(
                        publishedAt = now,
                        collectedAt = now,
                        lastVerifiedAt = now,
                    ),
                factcheck =
                    ResearchFactcheckDto(
                        status = "insufficient",
                        confidence = 0.58,
                        confidenceReasons = listOf("initial_fetch", "single_source"),
                        claims =
                            listOf(
                                ResearchClaimDto(
                                    claimText = "${req.keyword} 관련 보도 데이터가 제한적입니다.",
                                    verdict = "insufficient",
                                    evidenceIds = listOf("ev-1", "ev-2"),
                                ),
                            ),
                    ),
                trend =
                    ResearchTrendDto(
                        trendScore = 76,
                        velocity = 1.3,
                        regionRank = 4,
                    ),
            )

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
            items = listOf(item),
            summary =
                ResearchSummaryDto(
                    brief = "요약: ${req.keyword} 이슈는 모니터링 단계입니다.",
                    analystNote = "근거 수집량이 적어 추가 확인이 필요합니다.",
                    riskFlags = listOf("insufficient_evidence"),
                ),
            usage =
                ResearchUsageDto(
                    providerCalls = 1,
                    latencyMs = latencyMs,
                    cacheHit = false,
                ),
        )
    }
}
