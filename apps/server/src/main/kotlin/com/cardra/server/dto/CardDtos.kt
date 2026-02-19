package com.cardra.server.dto

import com.cardra.server.domain.CardStatus
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateCardRequest(
    @field:NotBlank
    val keyword: String,
    val tone: String = "neutral"
)

data class CardResponse(
    val id: UUID,
    val keyword: String,
    val cards: List<CardItem>,
    val status: CardStatus,
    val createdAt: Instant,
)

data class CardSummaryResponse(
    val id: UUID,
    val keyword: String,
    val status: CardStatus,
    val createdAt: Instant,
)

data class CardItem(
    val title: String,
    val body: String,
    val source: List<String>,
    val sourceAt: String
)
