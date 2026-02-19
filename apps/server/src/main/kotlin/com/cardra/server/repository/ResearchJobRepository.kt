package com.cardra.server.repository

import com.cardra.server.domain.ResearchJobEntity
import com.cardra.server.domain.ResearchJobStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ResearchJobRepository : JpaRepository<ResearchJobEntity, String> {
    fun findFirstByIdempotencyKeyOrderByCreatedAtDesc(idempotencyKey: String): ResearchJobEntity?

    fun findFirstByRequestKeyAndStatusOrderByUpdatedAtDesc(
        requestKey: String,
        status: ResearchJobStatus,
    ): ResearchJobEntity?
}
