package com.cardra.server.service.recommend

import com.cardra.server.dto.RecommendCandidate
import com.cardra.server.dto.RecommendEvent
import com.cardra.server.dto.RecommendEventRequest
import com.cardra.server.dto.RecommendEventResponse
import com.cardra.server.dto.RecommendKeywordRequest
import com.cardra.server.dto.RecommendKeywordResponse
import com.cardra.server.dto.RecommendStrategy
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Service
class RecommendationService {
    private val userHistory: ConcurrentMap<String, MutableList<String>> = ConcurrentHashMap()
    private val maxHistory = 30

    fun recommend(req: RecommendKeywordRequest): RecommendKeywordResponse {
        val startNs = Instant.now().toEpochMilli()
        val events = userHistory[req.userId] ?: emptyList()
        val hasHistory = events.isNotEmpty()
        val hasCurrentQuery = !req.currentQuery.isNullOrBlank()

        val strategy =
            when {
                hasHistory -> RecommendStrategy.PERSONALIZED
                hasCurrentQuery -> RecommendStrategy.SESSION_CONTEXT
                else -> RecommendStrategy.GLOBAL_POPULAR
            }

        val fallbackUsed = strategy != RecommendStrategy.PERSONALIZED
        val prefix = req.currentQuery?.trim().orEmpty()
        val categorySafe = req.categoryId?.trim().takeUnless { it.isNullOrBlank() } ?: "일반"
        val limit = req.limit.coerceIn(1, 20)

        val candidates =
            generateSequence {
                if (prefix.isBlank()) categorySafe else prefix
            }
                .take(limit)
                .mapIndexed { index, base ->
                    RecommendCandidate(
                        keyword =
                            if (req.currentQuery.isNullOrBlank()) {
                                "${categorySafe} 인사이트 #${index + 1}"
                            } else {
                                "$base 연관 #${index + 1}"
                            },
                        score = 0.95 - (index * 0.05).toDouble(),
                        reasons =
                            when (strategy) {
                                RecommendStrategy.PERSONALIZED -> listOf("personalized", "recency")
                                RecommendStrategy.SESSION_CONTEXT -> listOf("session_context", "recent_search")
                                else -> listOf("global_trending", "category_based")
                            },
                        source =
                            when (strategy) {
                                RecommendStrategy.PERSONALIZED -> "vector_personalized"
                                RecommendStrategy.SESSION_CONTEXT -> "session_similarity"
                                else -> "global_trend"
                            },
                    )
                }
                .filter { c ->
                    req.excludeKeywords.none { c.keyword.contains(it, ignoreCase = true) } &&
                        c.keyword.isNotBlank()
                }
                .toList()

        val latencyMs = Instant.now().toEpochMilli() - startNs

        return RecommendKeywordResponse(
            requestId = "req-${UUID.randomUUID()}",
            userId = req.userId,
            candidates = candidates,
            fallbackUsed = fallbackUsed,
            fallbackReason = strategy.name,
            strategy = strategy.name,
            modelVersion = "emb-v1.0",
            latencyMs = latencyMs,
        )
    }

    fun ingestEvents(req: RecommendEventRequest): RecommendEventResponse {
        val stored = userHistory.getOrPut(req.userId) { mutableListOf() }
        var accepted = 0
        var failed = 0

        req.events.forEach { event: RecommendEvent ->
            val keyword = event.keyword.trim()
            if (keyword.isBlank()) {
                failed += 1
                return@forEach
            }

            stored.add(keyword)
            if (stored.size > maxHistory) {
                while (stored.size > maxHistory) {
                    stored.removeAt(0)
                }
            }
            accepted += 1
        }

        return RecommendEventResponse(accepted = accepted, failed = failed)
    }
}
