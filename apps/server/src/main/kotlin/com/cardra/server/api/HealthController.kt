package com.cardra.server.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import javax.sql.DataSource

@RestController
@RequestMapping("/api/v1")
@Tag(name = "System", description = "System health and readiness APIs")
class HealthController(
    private val dataSource: DataSource,
) {
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Return server health summary")
    fun health(): ResponseEntity<Map<String, Any>> {
        val dbHealthy =
            try {
                dataSource.connection.use { it.isValid(2) }
            } catch (e: Exception) {
                false
            }
        val status = if (dbHealthy) "ok" else "degraded"
        val httpStatus = if (dbHealthy) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE
        return ResponseEntity.status(httpStatus).body(
            mapOf(
                "status" to status,
                "db" to if (dbHealthy) "ok" else "error",
                "timestamp" to Instant.now().toString(),
            ),
        )
    }
}
