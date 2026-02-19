package com.cardra.server.api

import com.cardra.server.dto.ImageGenerateRequest
import com.cardra.server.dto.ImageGenerateResponse
import com.cardra.server.dto.ImageProviderStatusResponse
import com.cardra.server.service.image.ImageGenerationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "Images", description = "Image generation and provider status APIs")
class ImageController(
    private val imageGenerationService: ImageGenerationService,
) {
    @GetMapping("/providers/status")
    @Operation(summary = "Get image provider status", description = "Return provider readiness and selected provider information")
    fun providerStatus(): ResponseEntity<ImageProviderStatusResponse> {
        return ResponseEntity.ok(imageGenerationService.providerStatus())
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate image", description = "Generate an image with selected provider and fallback policy")
    fun generate(
        @Valid @RequestBody req: ImageGenerateRequest,
    ): ResponseEntity<ImageGenerateResponse> {
        return ResponseEntity.ok(imageGenerationService.generate(req))
    }
}
