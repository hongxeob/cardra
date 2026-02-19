package com.cardra.server.service.image

import com.cardra.server.dto.ImageGenerateRequest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder

class OpenAiImageGeneratorTest {
    @Test
    fun `throws schema error when adapter is disabled`() {
        val config =
            OpenAiImageConfig().apply {
                enabled = false
                apiKey = "sk-test"
                model = "gpt-image-1"
            }
        val generator = OpenAiImageGenerator(config, RestTemplateBuilder())

        assertThrows(ImageGenerationSchemaError::class.java) {
            generator.generate(ImageGenerateRequest(prompt = "AI image"))
        }
    }

    @Test
    fun `throws schema error when api key is missing`() {
        val config =
            OpenAiImageConfig().apply {
                enabled = true
                apiKey = ""
                model = "gpt-image-1"
            }
        val generator = OpenAiImageGenerator(config, RestTemplateBuilder())

        assertThrows(ImageGenerationSchemaError::class.java) {
            generator.generate(ImageGenerateRequest(prompt = "AI image"))
        }
    }
}
