package com.cardra.server.exception

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import java.util.UUID

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CardNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(e: CardNotFoundException): ErrorBody {
        return ErrorBody(code = "NOT_FOUND", message = e.message ?: "Not found", retryable = false)
    }

    @ExceptionHandler(ResearchJobNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleResearchJobNotFound(e: ResearchJobNotFoundException): ErrorBody {
        return ErrorBody(
            code = "RESEARCH_JOB_NOT_FOUND",
            message = e.message ?: "Research job not found",
            retryable = false,
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(e: IllegalArgumentException): ErrorBody {
        return ErrorBody(code = "BAD_REQUEST", message = e.message ?: "Bad request", retryable = false)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ErrorBody {
        val msg = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field} ${it.defaultMessage}" }
        return ErrorBody(code = "VALIDATION_ERROR", message = msg, retryable = false)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAny(e: Exception): ErrorBody {
        return ErrorBody(
            code = "INTERNAL_ERROR",
            message = e.message ?: "Unexpected error",
            retryable = true,
            retryAfter = 5,
            traceId = "trace-${UUID.randomUUID()}",
        )
    }
}

data class ErrorBody(
    val code: String,
    val message: String,
    val retryable: Boolean = false,
    val retryAfter: Int? = null,
    val traceId: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val time: Instant = Instant.now(),
)
