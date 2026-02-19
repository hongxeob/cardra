package com.cardra.server.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class ImageGenerateRequest(
    @field:NotBlank
    val prompt: String,
    @field:Pattern(
        regexp = "^\\d{2,4}x\\d{2,4}$",
        message = "must match <width>x<height> (for example 1024x1024)",
    )
    val size: String = "1024x1024",
    val provider: String? = null,
)

data class ImageGenerateResponse(
    val status: String,
    val provider: String,
    val model: String,
    val mimeType: String,
    val imageBase64: String? = null,
    val imageUrl: String? = null,
    val usedFallback: Boolean = false,
)

data class ImageProviderStatusItem(
    val name: String,
    val enabled: Boolean,
    val apiKeyConfigured: Boolean,
    val model: String,
    val baseUrl: String,
    val selected: Boolean,
)

data class ImageProviderStatusResponse(
    val activeProvider: String,
    val providers: List<ImageProviderStatusItem>,
)
