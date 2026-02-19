package com.cardra.server.service.image

import com.cardra.server.dto.ImageGenerateRequest
import com.cardra.server.dto.ImageGenerateResponse
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FallbackImageGeneratorTest {
    private val req = ImageGenerateRequest(prompt = "AI agent visual")

    @Test
    fun `uses openai generator when provider is openai`() {
        val openAi: ImageGenerator = mockk()
        val gemini: ImageGenerator = mockk()
        val fallback: ImageGenerator = mockk()
        val providerConfig = ImageProviderConfig().apply { provider = "openai" }
        val fallbackConfig = ImageFallbackConfig().apply { allowStubFallback = true }
        val expected =
            ImageGenerateResponse(
                status = "completed",
                provider = "openai",
                model = "gpt-image-1",
                mimeType = "image/png",
                imageBase64 = "AAA",
                usedFallback = false,
            )
        val openAiReq: CapturingSlot<ImageGenerateRequest> = slot()
        every { openAi.generate(capture(openAiReq)) } returns expected

        val generator = FallbackImageGenerator(openAi, gemini, fallback, providerConfig, fallbackConfig)
        val actual = generator.generate(req)

        assertEquals(expected, actual)
        assertEquals("openai", openAiReq.captured.provider)
        verify(exactly = 1) { openAi.generate(any()) }
        verify(exactly = 0) { gemini.generate(any()) }
        verify(exactly = 0) { fallback.generate(any()) }
    }

    @Test
    fun `uses gemini generator when provider is nanobanana alias`() {
        val openAi: ImageGenerator = mockk()
        val gemini: ImageGenerator = mockk()
        val fallback: ImageGenerator = mockk()
        val providerConfig = ImageProviderConfig().apply { provider = "nano-banana" }
        val fallbackConfig = ImageFallbackConfig().apply { allowStubFallback = true }
        val expected =
            ImageGenerateResponse(
                status = "completed",
                provider = "gemini",
                model = "gemini-2.5-flash-image",
                mimeType = "image/png",
                imageBase64 = "AAA",
                usedFallback = false,
            )
        val geminiReq: CapturingSlot<ImageGenerateRequest> = slot()
        every { gemini.generate(capture(geminiReq)) } returns expected

        val generator = FallbackImageGenerator(openAi, gemini, fallback, providerConfig, fallbackConfig)
        val actual = generator.generate(req)

        assertEquals(expected, actual)
        assertEquals("gemini", geminiReq.captured.provider)
        verify(exactly = 0) { openAi.generate(any()) }
        verify(exactly = 1) { gemini.generate(any()) }
        verify(exactly = 0) { fallback.generate(any()) }
    }

    @Test
    fun `falls back when selected generator fails`() {
        val openAi: ImageGenerator = mockk()
        val gemini: ImageGenerator = mockk()
        val fallback: ImageGenerator = mockk()
        val providerConfig = ImageProviderConfig().apply { provider = "gemini" }
        val fallbackConfig = ImageFallbackConfig().apply { allowStubFallback = true }
        val expected =
            ImageGenerateResponse(
                status = "completed",
                provider = "stub",
                model = "placeholder",
                mimeType = "image/jpeg",
                imageUrl = "https://picsum.photos/seed/test/1024/1024",
                usedFallback = true,
            )
        every { gemini.generate(any()) } throws ImageGenerationTimeoutError("timeout")
        every { fallback.generate(req) } returns expected

        val generator = FallbackImageGenerator(openAi, gemini, fallback, providerConfig, fallbackConfig)
        val actual = generator.generate(req)

        assertEquals(expected, actual)
        assertTrue(actual.usedFallback)
        verify(exactly = 0) { openAi.generate(any()) }
        verify(exactly = 1) { gemini.generate(any()) }
        verify(exactly = 1) { fallback.generate(req) }
    }

    @Test
    fun `request provider override wins over config provider`() {
        val openAi: ImageGenerator = mockk()
        val gemini: ImageGenerator = mockk()
        val fallback: ImageGenerator = mockk()
        val providerConfig = ImageProviderConfig().apply { provider = "openai" }
        val fallbackConfig = ImageFallbackConfig().apply { allowStubFallback = true }
        val expected =
            ImageGenerateResponse(
                status = "completed",
                provider = "gemini",
                model = "gemini-2.5-flash-image",
                mimeType = "image/png",
                imageBase64 = "AAA",
                usedFallback = false,
            )
        val geminiReq: CapturingSlot<ImageGenerateRequest> = slot()
        every { gemini.generate(capture(geminiReq)) } returns expected

        val generator = FallbackImageGenerator(openAi, gemini, fallback, providerConfig, fallbackConfig)
        val actual = generator.generate(req.copy(provider = "gemini"))

        assertEquals(expected, actual)
        assertEquals("gemini", geminiReq.captured.provider)
        verify(exactly = 0) { openAi.generate(any()) }
        verify(exactly = 1) { gemini.generate(any()) }
    }

    @Test
    fun `throws primary error when fallback disabled`() {
        val openAi: ImageGenerator = mockk()
        val gemini: ImageGenerator = mockk()
        val fallback: ImageGenerator = mockk()
        val providerConfig = ImageProviderConfig().apply { provider = "openai" }
        val fallbackConfig = ImageFallbackConfig().apply { allowStubFallback = false }
        every { openAi.generate(any()) } throws ImageGenerationTimeoutError("timeout")

        val generator = FallbackImageGenerator(openAi, gemini, fallback, providerConfig, fallbackConfig)

        org.junit.jupiter.api.assertThrows<ImageGenerationTimeoutError> {
            generator.generate(req)
        }
        verify(exactly = 0) { fallback.generate(any()) }
    }
}
