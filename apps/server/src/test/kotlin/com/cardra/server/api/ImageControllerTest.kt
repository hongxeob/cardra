package com.cardra.server.api

import com.cardra.server.dto.ImageGenerateRequest
import com.cardra.server.dto.ImageGenerateResponse
import com.cardra.server.dto.ImageProviderStatusItem
import com.cardra.server.dto.ImageProviderStatusResponse
import com.cardra.server.exception.GlobalExceptionHandler
import com.cardra.server.service.image.ImageGenerationService
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ImageControllerTest {
    private val service: ImageGenerationService = mockk(relaxed = true)
    private val mvc: MockMvc =
        MockMvcBuilders.standaloneSetup(ImageController(service))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    private val mapper = ObjectMapper()

    @Test
    fun `provider status returns readiness`() {
        every { service.providerStatus() } returns
            ImageProviderStatusResponse(
                activeProvider = "gemini",
                providers =
                    listOf(
                        ImageProviderStatusItem(
                            name = "openai",
                            enabled = true,
                            apiKeyConfigured = true,
                            model = "gpt-image-1",
                            baseUrl = "https://api.openai.com",
                            selected = false,
                        ),
                        ImageProviderStatusItem(
                            name = "gemini",
                            enabled = true,
                            apiKeyConfigured = false,
                            model = "gemini-2.5-flash-image",
                            baseUrl = "https://generativelanguage.googleapis.com",
                            selected = true,
                        ),
                    ),
            )

        mvc.perform(get("/api/v1/images/providers/status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.activeProvider").value("gemini"))
            .andExpect(jsonPath("$.providers.length()").value(2))
    }

    @Test
    fun `generate image returns payload`() {
        every { service.generate(any()) } returns
            ImageGenerateResponse(
                status = "completed",
                provider = "openai",
                model = "gpt-image-1",
                mimeType = "image/png",
                imageBase64 = "iVBORw0KGgoAAA...",
                usedFallback = false,
            )

        mvc.perform(
            post("/api/v1/images/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    mapper.writeValueAsString(
                        ImageGenerateRequest(
                            prompt = "AI agent concept art",
                            size = "1024x1024",
                            provider = "gemini",
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("completed"))
            .andExpect(jsonPath("$.provider").value("openai"))
            .andExpect(jsonPath("$.mimeType").value("image/png"))
    }

    @Test
    fun `generate image validation fail`() {
        mvc.perform(
            post("/api/v1/images/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"\"}"),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `generate image validation fail when size format is invalid`() {
        mvc.perform(
            post("/api/v1/images/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"hello\",\"size\":\"square\"}"),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
    }

    @Test
    fun `generate image returns bad request on unsupported provider`() {
        every { service.generate(any()) } throws IllegalArgumentException("provider must be one of: openai, gemini, nano-banana")

        mvc.perform(
            post("/api/v1/images/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prompt\":\"hello\",\"provider\":\"midjourney\"}"),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
    }
}
