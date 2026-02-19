package com.cardra.server.api

import com.cardra.server.dto.CardResponse
import com.cardra.server.dto.CreateCardRequest
import com.cardra.server.service.CardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@RequestMapping("/api/v1/cards")
@Tag(name = "Cards", description = "Card generation and retrieval APIs")
class CardController(
    private val cardService: CardService,
) {
    @PostMapping("/generate")
    @Operation(summary = "Generate cards", description = "Generate card news from a keyword request")
    fun generate(
        @Valid @RequestBody req: CreateCardRequest,
    ): ResponseEntity<CardResponse> {
        val result = cardService.createCard(req)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get card", description = "Fetch a generated card by id")
    fun get(
        @PathVariable id: UUID,
    ): ResponseEntity<CardResponse> {
        return ResponseEntity.ok(cardService.getCard(id))
    }
}
