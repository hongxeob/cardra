package com.cardra.server.service.research

import com.cardra.server.domain.ResearchJobEntity
import com.cardra.server.domain.ResearchJobStatus
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
import com.cardra.server.repository.ResearchJobRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future

private const val CACHE_TTL_SEC = 180

@Service
class ResearchJobService(
    private val researchService: ResearchService,
    private val researchJobRepository: ResearchJobRepository,
    private val objectMapper: ObjectMapper,
) {
    private val jobExecutor = Executors.newFixedThreadPool(2)
    private val runningFutures = ConcurrentHashMap<String, Future<*>>()

    fun createJob(req: ResearchJobCreateRequest): ResearchJobCreateResponse {
        val idempotencyKey = req.idempotencyKey?.trim()?.takeIf { it.isNotBlank() }
        val runRequest = req.toRunRequest()
        val requestKey = cacheKey(runRequest)

        if (idempotencyKey != null) {
            researchJobRepository.findFirstByIdempotencyKeyOrderByCreatedAtDesc(idempotencyKey)?.let { replay ->
                return replay.toCreateResponse()
            }
        }

        val now = Instant.now()
        val traceId = UUID.randomUUID().toString()

        val cachedJob =
            researchJobRepository.findFirstByRequestKeyAndStatusOrderByUpdatedAtDesc(
                requestKey,
                ResearchJobStatus.COMPLETED,
            )

        if (!cachedJob?.resultJson.isNullOrBlank()) {
            val saved =
                researchJobRepository.save(
                    ResearchJobEntity(
                        keyword = req.keyword,
                        language = req.language,
                        country = req.country,
                        timeRange = req.timeRange,
                        maxItems = req.maxItems,
                        summaryLevel = req.summaryLevel,
                        factcheckMode = req.factcheckMode,
                        idempotencyKey = idempotencyKey,
                        requestKey = requestKey,
                        traceId = traceId,
                        status = ResearchJobStatus.COMPLETED,
                        resultJson = cachedJob?.resultJson,
                        errorJson = null,
                        fromCache = true,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            return saved.toCreateResponse()
        }

        val saved =
            researchJobRepository.save(
                ResearchJobEntity(
                    keyword = req.keyword,
                    language = req.language,
                    country = req.country,
                    timeRange = req.timeRange,
                    maxItems = req.maxItems,
                    summaryLevel = req.summaryLevel,
                    factcheckMode = req.factcheckMode,
                    idempotencyKey = idempotencyKey,
                    requestKey = requestKey,
                    traceId = traceId,
                    status = ResearchJobStatus.QUEUED,
                    createdAt = now,
                    updatedAt = now,
                ),
            )

        runningFutures[saved.id] =
            CompletableFuture.runAsync(
                {
                    runJob(saved.id, runRequest, traceId)
                },
                jobExecutor,
            )

        return saved.toCreateResponse()
    }

    private fun runJob(
        jobId: String,
        req: ResearchRunRequest,
        traceId: String,
    ) {
        val state = researchJobRepository.findById(jobId).orElse(null) ?: return
        if (state.status == ResearchJobStatus.CANCELLED) {
            return
        }

        state.status = ResearchJobStatus.RUNNING
        state.updatedAt = Instant.now()
        researchJobRepository.save(state)

        try {
            Thread.sleep(20)
            val result = researchService.runResearch(req, traceId)
            val cancelled = researchJobRepository.findById(jobId).orElse(null)?.status == ResearchJobStatus.CANCELLED
            if (cancelled) {
                state.status = ResearchJobStatus.CANCELLED
            } else {
                state.resultJson =
                    objectMapper.writeValueAsString(
                        result.copy(
                            usage = result.usage?.copy(cacheHit = false),
                        ),
                    )
                state.errorJson = null
                state.status = ResearchJobStatus.COMPLETED
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            state.status = ResearchJobStatus.CANCELLED
            state.errorJson = null
        } catch (e: Exception) {
            val retryAfter = if (e is IllegalArgumentException) null else 5
            val retryable = retryAfter != null

            state.errorJson =
                objectMapper.writeValueAsString(
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
                    ),
                )
            state.resultJson = null
            state.status = ResearchJobStatus.FAILED
        } finally {
            state.updatedAt = Instant.now()
            researchJobRepository.save(state)
            runningFutures.remove(jobId)
        }
    }

    private fun ResearchJobEntity.toCreateResponse(): ResearchJobCreateResponse =
        ResearchJobCreateResponse(
            jobId = id,
            status = status.name.lowercase(),
            traceId = traceId,
        )

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
        val state = researchJobRepository.findById(jobId).orElseThrow { ResearchJobNotFoundException(jobId) }

        return ResearchJobStatusResponse(
            jobId = jobId,
            status = state.status.name.lowercase(),
            createdAt = state.createdAt.toString(),
            updatedAt = state.updatedAt.toString(),
            error = parseError(state.errorJson),
        )
    }

    fun getResult(jobId: String): ResearchJobResultResponse {
        val state = researchJobRepository.findById(jobId).orElseThrow { ResearchJobNotFoundException(jobId) }
        val parsedResult = parseResult(state.resultJson)

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
                parsedResult?.copy(
                    usage =
                        parsedResult.usage?.copy(
                            cacheHit = state.fromCache,
                        ),
                ),
            error = parseError(state.errorJson),
            cache = cacheInfo,
        )
    }

    fun cancelJob(jobId: String): ResearchJobCancelResponse {
        val state = researchJobRepository.findById(jobId).orElseThrow { ResearchJobNotFoundException(jobId) }
        val current = state.status

        val canCancel = current == ResearchJobStatus.QUEUED || current == ResearchJobStatus.RUNNING
        val cancelled =
            if (!canCancel) {
                false
            } else {
                val cancelledInMemory = runningFutures[jobId]?.cancel(true) ?: true
                if (cancelledInMemory) {
                    state.status = ResearchJobStatus.CANCELLED
                    state.updatedAt = Instant.now()
                    researchJobRepository.save(state)
                    runningFutures.remove(jobId)
                }
                cancelledInMemory
            }

        return ResearchJobCancelResponse(
            jobId = jobId,
            status = state.status.name.lowercase(),
            cancelled = cancelled,
        )
    }

    private fun parseResult(json: String?): ResearchRunResponse? {
        if (json.isNullOrBlank()) {
            return null
        }
        return objectMapper.readValue(json, ResearchRunResponse::class.java)
    }

    private fun parseError(json: String?): ResearchJobErrorResponse? {
        if (json.isNullOrBlank()) {
            return null
        }
        return objectMapper.readValue(json, ResearchJobErrorResponse::class.java)
    }
}
