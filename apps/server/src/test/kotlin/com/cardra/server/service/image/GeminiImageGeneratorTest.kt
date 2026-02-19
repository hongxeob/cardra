package com.cardra.server.service.image

import com.cardra.server.dto.ImageGenerateRequest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder

class GeminiImageGeneratorTest {
    @Test
    fun `throws schema error when adapter is disabled`() {
        val config =
            GeminiImageConfig().apply {
                enabled = false
                apiKey = "gemini-key"
                model = "gemini-2.5-flash-image"
            }
        val generator = GeminiImageGenerator(config, RestTemplateBuilder())

        assertThrows(ImageGenerationSchemaError::class.java) {
            generator.generate(ImageGenerateRequest(prompt = "AI image"))
        }
    }

    @Test
    fun `throws schema error when api key is missing`() {
        val config =
            GeminiImageConfig().apply {
                enabled = true
                apiKey = ""
                model = "gemini-2.5-flash-image"
            }
        val generator = GeminiImageGenerator(config, RestTemplateBuilder())

        assertThrows(ImageGenerationSchemaError::class.java) {
            generator.generate(ImageGenerateRequest(prompt = "AI image"))
        }
    }
}
