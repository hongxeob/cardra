package com.cardra.server.service.image

import com.cardra.server.dto.ImageGenerateRequest
import com.cardra.server.dto.ImageGenerateResponse
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImageGenerationServiceTest {
    private fun newService(generator: ImageGenerator): ImageGenerationService {
        val providerConfig = ImageProviderConfig().apply { provider = "openai" }
        val openAiConfig =
            OpenAiImageConfig().apply {
                enabled = true
                apiKey = "sk-test"
                model = "gpt-image-1"
                baseUrl = "https://api.openai.com"
            }
        val geminiConfig =
            GeminiImageConfig().apply {
                enabled = true
                apiKey = "gemini-test"
                model = "gemini-2.5-flash-image"
                baseUrl = "https://generativelanguage.googleapis.com"
            }
        return ImageGenerationService(generator, providerConfig, openAiConfig, geminiConfig)
    }

    @Test
    fun `service trims prompt and normalizes provider`() {
        val generator: ImageGenerator = mockk()
        val service = newService(generator)
        val captured: CapturingSlot<ImageGenerateRequest> = slot()
        every { generator.generate(capture(captured)) } returns
            ImageGenerateResponse(
                status = "completed",
                provider = "stub",
                model = "placeholder",
                mimeType = "image/jpeg",
                imageUrl = "https://picsum.photos/seed/x/1024/1024",
                usedFallback = true,
            )

        val response =
            service.generate(
                ImageGenerateRequest(
                    prompt = "  AI cover  ",
                    provider = "nano banana",
                ),
            )
        assertEquals("completed", response.status)
        assertEquals("AI cover", captured.captured.prompt)
        assertEquals("gemini", captured.captured.provider)
    }

    @Test
    fun `service rejects blank prompt`() {
        val generator: ImageGenerator = mockk()
        val service = newService(generator)

        assertThrows(IllegalArgumentException::class.java) {
            service.generate(ImageGenerateRequest(prompt = "   "))
        }
    }

    @Test
    fun `service rejects unsupported provider`() {
        val generator: ImageGenerator = mockk()
        val service = newService(generator)

        assertThrows(IllegalArgumentException::class.java) {
            service.generate(ImageGenerateRequest(prompt = "test", provider = "midjourney"))
        }
    }

    @Test
    fun `provider status returns key readiness without secrets`() {
        val generator: ImageGenerator = mockk()
        val service = newService(generator)

        val status = service.providerStatus()

        assertEquals("openai", status.activeProvider)
        assertEquals(2, status.providers.size)
        assertTrue(status.providers.any { it.name == "openai" && it.apiKeyConfigured })
    }
}
