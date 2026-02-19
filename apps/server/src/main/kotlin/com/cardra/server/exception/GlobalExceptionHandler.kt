package com.cardra.server.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<ErrorBody> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorBody(code = "BAD_REQUEST", message = e.message ?: "Bad request"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleValidation(e: MethodArgumentNotValidException): ErrorBody {
        val msg = e.bindingResult.fieldErrors.joinToString(", ") { "${'$'}{it.field} ${'$'}{it.defaultMessage}" }
        return ErrorBody(code = "VALIDATION_ERROR", message = msg)
    }

    @ExceptionHandler(Exception::class)
    fun handleAny(e: Exception): ResponseEntity<ErrorBody> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorBody(code = "INTERNAL_ERROR", message = e.message ?: "Unexpected error"))
    }
}

data class ErrorBody(
    val code: String,
    val message: String,
    val time: String = Instant.now().toString(),
)
