package com.cardra.server.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "research_jobs",
    indexes = [
        Index(name = "idx_research_jobs_idempotency", columnList = "idempotencyKey"),
        Index(name = "idx_research_jobs_request_status", columnList = "requestKey,status"),
    ],
)
class ResearchJobEntity(
    @Id
    @Column(length = 36, nullable = false)
    var id: String = UUID.randomUUID().toString(),
    @Column(nullable = false)
    var keyword: String = "",
    @Column(nullable = false)
    var language: String = "ko",
    @Column(nullable = false)
    var country: String = "KR",
    @Column(nullable = false)
    var timeRange: String = "24h",
    @Column(nullable = false)
    var maxItems: Int = 5,
    @Column(nullable = false)
    var summaryLevel: String = "standard",
    @Column(nullable = false)
    var factcheckMode: String = "strict",
    @Column(length = 128)
    var idempotencyKey: String? = null,
    @Column(length = 512, nullable = false)
    var requestKey: String = "",
    @Column(length = 36, nullable = false)
    var traceId: String = "",
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ResearchJobStatus = ResearchJobStatus.QUEUED,
    @Column(columnDefinition = "TEXT")
    var resultJson: String? = null,
    @Column(columnDefinition = "TEXT")
    var errorJson: String? = null,
    @Column(nullable = false)
    var fromCache: Boolean = false,
    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),
    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),
)

enum class ResearchJobStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED,
}
