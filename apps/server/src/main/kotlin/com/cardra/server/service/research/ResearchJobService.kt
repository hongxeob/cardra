package com.cardra.server.service.research

import com.cardra.server.dto.ResearchJobCacheDto
import com.cardra.server.dto.ResearchJobCancelResponse
import com.cardra.server.dto.ResearchJobCreateRequest
import com.cardra.server.dto.ResearchJobCreateResponse
import com.cardra.server.dto.ResearchJobErrorResponse
import com.cardra.server.dto.ResearchJobResultResponse
import com.cardra.server.dto.ResearchJobStatusResponse
import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchRunResponse
import com.cardra.server.dto.ResearchUsageDto
import com.cardra.server.exception.ResearchJobNotFoundException
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Executors
import java.util.concurrent.Future

private const val CACHE_TTL_SEC = 180

enum class ResearchJobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
}

private data class ResearchJobState(
    val request: ResearchJobCreateRequest,
    val requestKey: String,
    val traceId: String,
    val createdAt: Instant,
    var status: ResearchJobStatus,
    var updatedAt: Instant,
    var result: ResearchRunResponse? = null,
    var error: ResearchJobErrorResponse? = null,
    var future: Future<*>? = null,
    var fromCache: Boolean = false,
)

@Service
class ResearchJobService(
    private val researchService: ResearchService,
) {
    private val jobs: ConcurrentMap<String, ResearchJobState> = ConcurrentHashMap()
    private val idempotencyIndex: ConcurrentMap<String, String> = ConcurrentHashMap()
    private val cache: ConcurrentMap<String, ResearchRunResponse> = ConcurrentHashMap()
    private val jobExecutor = Executors.newFixedThreadPool(2)

    fun createJob(req: ResearchJobCreateRequest): ResearchJobCreateResponse {
        val idempotencyKey = req.idempotencyKey?.trim().orEmpty()
        val runRequest = req.toRunRequest()
        val requestKey = cacheKey(runRequest)

        if (idempotencyKey.isNotBlank()) {
            val replayJobId = idempotencyIndex[idempotencyKey]
            if (replayJobId != null) {
                val replay = jobs[replayJobId] ?: throw ResearchJobNotFoundException(replayJobId)
                return ResearchJobCreateResponse(
                    jobId = replayJobId,
                    status = replay.status.name.lowercase(),
                    traceId = replay.traceId,
                )
            }
        }

        val jobId = UUID.randomUUID().toString()
        val now = Instant.now()
        val traceId = UUID.randomUUID().toString()

        val cachedResponse = cache[requestKey]
        if (cachedResponse != null) {
            val state =
                ResearchJobState(
                    request = req,
                    requestKey = requestKey,
                    traceId = traceId,
                    createdAt = now,
                    status = ResearchJobStatus.COMPLETED,
                    updatedAt = now,
                    result = cachedResponse,
                    fromCache = true,
                )
            jobs[jobId] = state
            cacheIndex(idempotencyKey, jobId)

            return ResearchJobCreateResponse(
                jobId = jobId,
                status = state.status.name.lowercase(),
                traceId = traceId,
            )
        }

        val state =
            ResearchJobState(
                request = req,
                requestKey = requestKey,
                traceId = traceId,
                createdAt = now,
                status = ResearchJobStatus.QUEUED,
                updatedAt = now,
            )
        jobs[jobId] = state

        state.future =
            CompletableFuture.runAsync(
                {
                    runJob(jobId, runRequest, traceId, requestKey)
                },
                jobExecutor,
            )

        cacheIndex(idempotencyKey, jobId)

        return ResearchJobCreateResponse(
            jobId = jobId,
            status = state.status.name.lowercase(),
            traceId = traceId,
        )
    }

    private fun runJob(
        jobId: String,
        req: ResearchRunRequest,
        traceId: String,
        cacheKey: String,
    ) {
        val state = jobs[jobId] ?: return
        state.status = ResearchJobStatus.RUNNING
        state.updatedAt = Instant.now()

        try {
            Thread.sleep(20)
            val result = researchService.runResearch(req, traceId)
            state.result = result.copy(usage = result.usage?.copy(cacheHit = false))
            state.status = ResearchJobStatus.COMPLETED
            cache[cacheKey] = result
        } catch (e: Exception) {
            val retryAfter = if (e is IllegalArgumentException) null else 5
            val retryable = retryAfter != null

            state.error =
                ResearchJobErrorResponse(
                    code = "RESEARCH_JOB_FAILED",
                    message = e.message ?: "Unexpected error",
                    retryable = retryable,
                    retryAfter = retryAfter,
                    traceId = traceId,
                    usage =
                        ResearchUsageDto(
                            providerCalls = 1,
                            latencyMs = 0,
                            cacheHit = false,
                        ),
                )
            state.status = ResearchJobStatus.FAILED
        } finally {
            state.updatedAt = Instant.now()
        }
    }

    private fun cacheIndex(
        key: String,
        jobId: String,
    ) {
        if (key.isNotBlank()) {
            idempotencyIndex[key] = jobId
        }
    }

    private fun cacheKey(req: ResearchRunRequest): String {
        return "${req.keyword}|${req.language}|${req.country}|${req.timeRange}|${req.maxItems}|${req.summaryLevel}|${req.factcheckMode}"
    }

    private fun ResearchJobCreateRequest.toRunRequest(): ResearchRunRequest =
        ResearchRunRequest(
            keyword = keyword,
            language = language,
            country = country,
            timeRange = timeRange,
            maxItems = maxItems,
            summaryLevel = summaryLevel,
            factcheckMode = factcheckMode,
        )

    fun getStatus(jobId: String): ResearchJobStatusResponse {
        val state = jobs[jobId] ?: throw ResearchJobNotFoundException(jobId)

        return ResearchJobStatusResponse(
            jobId = jobId,
            status = state.status.name.lowercase(),
            createdAt = state.createdAt.toString(),
            updatedAt = state.updatedAt.toString(),
            error = state.error,
        )
    }

    fun getResult(jobId: String): ResearchJobResultResponse {
        val state = jobs[jobId] ?: throw ResearchJobNotFoundException(jobId)

        val cacheInfo =
            if (state.status == ResearchJobStatus.COMPLETED && state.fromCache) {
                ResearchJobCacheDto(
                    hit = true,
                    ttlSec = CACHE_TTL_SEC,
                )
            } else {
                null
            }

        return ResearchJobResultResponse(
            jobId = jobId,
            status = state.status.name.lowercase(),
            result =
                state.result?.copy(
                    usage =
                        state.result?.usage?.copy(
                            cacheHit = state.fromCache,
                        ),
                ),
            error = state.error,
            cache = cacheInfo,
        )
    }

    fun cancelJob(jobId: String): ResearchJobCancelResponse {
        val state = jobs[jobId] ?: throw ResearchJobNotFoundException(jobId)
        val current = state.status

        val cancelled =
            (current == ResearchJobStatus.QUEUED || current == ResearchJobStatus.RUNNING) &&
                (state.future?.cancel(true) ?: false)

        if (cancelled) {
            state.status = ResearchJobStatus.CANCELLED
            state.updatedAt = Instant.now()
        }

        return ResearchJobCancelResponse(
            jobId = jobId,
            status = state.status.name.lowercase(),
            cancelled = cancelled,
        )
    }
}
