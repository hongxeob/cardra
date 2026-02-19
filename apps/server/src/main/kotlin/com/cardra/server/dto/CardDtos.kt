package com.cardra.server.dto

import com.cardra.server.domain.CardStatus
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateCardRequest(
    @field:NotBlank
    val keyword: String,
    val tone: String = "neutral",
)

data class CardResponse(
    val id: UUID,
    val keyword: String,
    val cards: List<CardItem>,
    val status: CardStatus,
    val createdAt: Instant,
)

data class CardItem(
    val title: String,
    val body: String,
    val source: List<String>,
    val sourceAt: String,
    val variant: String = "standard",
    val style: CardStyle = CardStyle(),
    val media: CardMedia? = null,
    val cta: CardCta? = null,
    val tags: List<String> = emptyList(),
    val imageHint: String? = null,
)

data class CardStyle(
    val tone: String = "neutral",
    val layout: String = "default",
    val emphasis: String = "balanced",
)

data class CardMedia(
    val imageUrl: String? = null,
    val imageType: String = "illustration",
    val altText: String? = null,
)

data class CardCta(
    val label: String,
    val actionType: String = "open",
    val target: String? = null,
)

data class CardSummaryResponse(
    val id: java.util.UUID,
    val keyword: String,
    val status: CardStatus,
    val createdAt: java.time.Instant,
)
