package com.cardra.server.api

import com.cardra.server.dto.ResearchJobCancelResponse
import com.cardra.server.dto.ResearchJobCreateRequest
import com.cardra.server.dto.ResearchJobCreateResponse
import com.cardra.server.dto.ResearchJobResultResponse
import com.cardra.server.dto.ResearchJobStatusResponse
import com.cardra.server.dto.ResearchRunRequest
import com.cardra.server.dto.ResearchRunResponse
import com.cardra.server.service.research.ResearchJobService
import com.cardra.server.service.research.ResearchService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/research")
class ResearchController(
    private val researchService: ResearchService,
    private val jobService: ResearchJobService,
) {
    @PostMapping("/run")
    fun run(
        @Valid @RequestBody req: ResearchRunRequest,
    ): ResearchRunResponse {
        return researchService.runResearch(req, UUID.randomUUID().toString())
    }

    @PostMapping("/jobs")
    fun createJob(
        @Valid @RequestBody req: ResearchJobCreateRequest,
    ): ResponseEntity<ResearchJobCreateResponse> {
        val response = jobService.createJob(req)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response)
    }

    @GetMapping("/jobs/{jobId}")
    fun getJobStatus(
        @PathVariable jobId: String,
    ): ResearchJobStatusResponse {
        return jobService.getStatus(jobId)
    }

    @GetMapping("/jobs/{jobId}/result")
    fun getResult(
        @PathVariable jobId: String,
    ): ResearchJobResultResponse {
        return jobService.getResult(jobId)
    }

    @PostMapping("/jobs/{jobId}/cancel")
    fun cancelJob(
        @PathVariable jobId: String,
    ): ResponseEntity<ResearchJobCancelResponse> {
        val response = jobService.cancelJob(jobId)
        return ResponseEntity.ok(response)
    }
}
