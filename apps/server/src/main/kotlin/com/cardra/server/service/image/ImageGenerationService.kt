package com.cardra.server.service.image

import com.cardra.server.dto.ImageGenerateRequest
import com.cardra.server.dto.ImageGenerateResponse
import com.cardra.server.dto.ImageProviderStatusItem
import com.cardra.server.dto.ImageProviderStatusResponse
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.Duration

sealed class ImageGenerationError(message: String) : RuntimeException(message)

class ImageGenerationTimeoutError(message: String) : ImageGenerationError(message)

class ImageGenerationRateLimitError(message: String) : ImageGenerationError(message)

class ImageGenerationUpstreamError(message: String) : ImageGenerationError(message)

class ImageGenerationSchemaError(message: String) : ImageGenerationError(message)

interface ImageGenerator {
    fun generate(req: ImageGenerateRequest): ImageGenerateResponse
}

private fun normalizeProvider(raw: String?): String? {
    val value = raw?.trim()?.lowercase().orEmpty()
    if (value.isBlank()) {
        return null
    }
    return when (value) {
        "openai" -> "openai"
        "gemini", "nano-banana", "nanobanana", "nano banana" -> "gemini"
        else -> null
    }
}

@Component
@ConfigurationProperties(prefix = "cardra.image")
class ImageProviderConfig {
    var provider: String = "openai"
}

@Component
@ConfigurationProperties(prefix = "cardra.image")
class ImageFallbackConfig {
    var allowStubFallback: Boolean = true
}

@Component
@ConfigurationProperties(prefix = "cardra.image.openai")
class OpenAiImageConfig {
    var enabled: Boolean = false
    var baseUrl: String = "https://api.openai.com"
    var apiKey: String = ""
    var model: String = "gpt-image-1"
    var timeoutSeconds: Long = 30
}

@Component
@ConfigurationProperties(prefix = "cardra.image.gemini")
class GeminiImageConfig {
    var enabled: Boolean = false
    var baseUrl: String = "https://generativelanguage.googleapis.com"
    var apiKey: String = ""
    var model: String = "gemini-2.5-flash-image"
    var timeoutSeconds: Long = 30
}

@Component("openAiImageGenerator")
class OpenAiImageGenerator(
    private val config: OpenAiImageConfig,
    restTemplateBuilder: RestTemplateBuilder,
) : ImageGenerator {
    private val logger = LoggerFactory.getLogger(OpenAiImageGenerator::class.java)
    private val restTemplate: RestTemplate =
        restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .readTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .build()

    override fun generate(req: ImageGenerateRequest): ImageGenerateResponse {
        if (!config.enabled) {
            throw ImageGenerationSchemaError("OpenAI image generator is disabled")
        }
        if (config.apiKey.isBlank()) {
            throw ImageGenerationSchemaError("OPENAI_API_KEY is required when OpenAI image generator is enabled")
        }
        if (config.model.isBlank()) {
            throw ImageGenerationSchemaError("OpenAI image model is required when enabled")
        }
        logger.info(
            "image_openai_request: model={} size={} promptLength={}",
            config.model,
            req.size,
            req.prompt.trim().length,
        )

        val endpoint = "${config.baseUrl.trimEnd('/')}/v1/images/generations"
        val request =
            RequestEntity
                .post(URI(endpoint))
                .header("Authorization", "Bearer ${config.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "model" to config.model,
                        "prompt" to req.prompt.trim(),
                        "size" to req.size,
                    ),
                )

        val response =
            try {
                restTemplate.exchange(request, OpenAiImageResponse::class.java)
            } catch (e: RestClientResponseException) {
                logger.warn(
                    "image_openai_http_error: status={} statusText={} body={}",
                    e.statusCode.value(),
                    e.statusText,
                    e.responseBodyAsString.take(300),
                )
                throw mapHttpError(e)
            } catch (e: Exception) {
                logger.error("image_openai_transport_error", e)
                throw ImageGenerationTimeoutError("OpenAI image generation failed")
            }

        if (!response.statusCode.is2xxSuccessful) {
            throw ImageGenerationUpstreamError("OpenAI image generation returned status ${response.statusCode}")
        }
        val firstData =
            response.body?.data?.firstOrNull()
                ?: throw ImageGenerationSchemaError("OpenAI image response has no data")

        val base64 = firstData.b64Json?.takeIf { it.isNotBlank() }
        val url = firstData.url?.takeIf { it.isNotBlank() }
        if (base64 == null && url == null) {
            throw ImageGenerationSchemaError("OpenAI image response has no b64_json or url")
        }
        logger.info(
            "image_openai_success: model={} hasBase64={} hasUrl={}",
            config.model,
            base64 != null,
            url != null,
        )

        return ImageGenerateResponse(
            status = "completed",
            provider = "openai",
            model = config.model,
            mimeType = "image/png",
            imageBase64 = base64,
            imageUrl = url,
            usedFallback = false,
        )
    }

    private fun mapHttpError(e: RestClientResponseException): ImageGenerationError {
        val status = e.statusCode.value()
        return when {
            status == 429 ->
                ImageGenerationRateLimitError("OpenAI image generation failed: ${e.statusCode} ${e.statusText}")
            status in 500..599 ->
                ImageGenerationUpstreamError("OpenAI image generation failed: ${e.statusCode} ${e.statusText}")
            else ->
                ImageGenerationSchemaError("OpenAI image request failed: ${e.statusCode} ${e.statusText}")
        }
    }
}

@Component("geminiImageGenerator")
class GeminiImageGenerator(
    private val config: GeminiImageConfig,
    restTemplateBuilder: RestTemplateBuilder,
) : ImageGenerator {
    private val logger = LoggerFactory.getLogger(GeminiImageGenerator::class.java)
    private val restTemplate: RestTemplate =
        restTemplateBuilder
            .connectTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .readTimeout(Duration.ofSeconds(config.timeoutSeconds))
            .build()

    override fun generate(req: ImageGenerateRequest): ImageGenerateResponse {
        if (!config.enabled) {
            throw ImageGenerationSchemaError("Gemini image generator is disabled")
        }
        if (config.apiKey.isBlank()) {
            throw ImageGenerationSchemaError("GEMINI_API_KEY is required when Gemini image generator is enabled")
        }
        if (config.model.isBlank()) {
            throw ImageGenerationSchemaError("Gemini image model is required when enabled")
        }

        val endpoint = "${config.baseUrl.trimEnd('/')}/v1beta/models/${config.model}:generateContent"
        val request =
            RequestEntity
                .post(URI(endpoint))
                .header("x-goog-api-key", config.apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                    mapOf(
                        "contents" to
                            listOf(
                                mapOf(
                                    "parts" to
                                        listOf(
                                            mapOf("text" to req.prompt.trim()),
                                        ),
                                ),
                            ),
                    ),
                )

        val response =
            try {
                restTemplate.exchange(request, GeminiGenerateContentResponse::class.java)
            } catch (e: RestClientResponseException) {
                logger.warn(
                    "image_gemini_http_error: status={} statusText={} body={}",
                    e.statusCode.value(),
                    e.statusText,
                    e.responseBodyAsString.take(300),
                )
                throw mapHttpError(e)
            } catch (e: Exception) {
                logger.error("image_gemini_transport_error", e)
                throw ImageGenerationTimeoutError("Gemini image generation failed")
            }

        if (!response.statusCode.is2xxSuccessful) {
            throw ImageGenerationUpstreamError("Gemini image generation returned status ${response.statusCode}")
        }

        val parts = response.body?.candidates?.firstOrNull()?.content?.parts.orEmpty()
        val imagePart =
            parts.firstOrNull { it.inlineData?.data?.isNotBlank() == true }
                ?: throw ImageGenerationSchemaError("Gemini image response has no inline image data")
        val inlineData =
            imagePart.inlineData
                ?: throw ImageGenerationSchemaError("Gemini image response is missing inlineData")

        return ImageGenerateResponse(
            status = "completed",
            provider = "gemini",
            model = config.model,
            mimeType = inlineData.mimeType ?: "image/png",
            imageBase64 = inlineData.data,
            imageUrl = null,
            usedFallback = false,
        )
    }

    private fun mapHttpError(e: RestClientResponseException): ImageGenerationError {
        val status = e.statusCode.value()
        return when {
            status == 429 ->
                ImageGenerationRateLimitError("Gemini image generation failed: ${e.statusCode} ${e.statusText}")
            status in 500..599 ->
                ImageGenerationUpstreamError("Gemini image generation failed: ${e.statusCode} ${e.statusText}")
            else ->
                ImageGenerationSchemaError("Gemini image request failed: ${e.statusCode} ${e.statusText}")
        }
    }
}

@Component("stubImageGenerator")
class StubImageGenerator : ImageGenerator {
    override fun generate(req: ImageGenerateRequest): ImageGenerateResponse {
        val seed = req.prompt.trim().ifBlank { "cardra" }.hashCode().toString()
        return ImageGenerateResponse(
            status = "completed",
            provider = "stub",
            model = "placeholder",
            mimeType = "image/jpeg",
            imageUrl = "https://picsum.photos/seed/$seed/1024/1024",
            usedFallback = true,
        )
    }
}

@Component("fallbackImageGenerator")
class FallbackImageGenerator(
    @Qualifier("openAiImageGenerator")
    private val openAi: ImageGenerator,
    @Qualifier("geminiImageGenerator")
    private val gemini: ImageGenerator,
    @Qualifier("stubImageGenerator")
    private val fallback: ImageGenerator,
    private val providerConfig: ImageProviderConfig,
    private val fallbackConfig: ImageFallbackConfig,
) : ImageGenerator {
    private val logger = LoggerFactory.getLogger(FallbackImageGenerator::class.java)

    private fun selectProvider(reqProvider: String?): String {
        return normalizeProvider(reqProvider)
            ?: normalizeProvider(providerConfig.provider)
            ?: "openai"
    }

    private fun selectPrimary(provider: String): ImageGenerator {
        return if (provider == "gemini") gemini else openAi
    }

    override fun generate(req: ImageGenerateRequest): ImageGenerateResponse {
        val provider = selectProvider(req.provider)
        val primary = selectPrimary(provider)
        return try {
            primary.generate(req.copy(provider = provider))
        } catch (e: ImageGenerationError) {
            if (!fallbackConfig.allowStubFallback) {
                logger.warn("image_fallback_disabled: provider={} reason={}", provider, e::class.simpleName)
                throw e
            }
            logger.warn("image_fallback_used: provider={} reason={} prompt={}", provider, e::class.simpleName, req.prompt)
            fallback.generate(req)
        } catch (e: Exception) {
            if (!fallbackConfig.allowStubFallback) {
                logger.warn("image_fallback_disabled: provider={} reason=UNKNOWN", provider)
                throw ImageGenerationUpstreamError("Image generation failed unexpectedly")
            }
            logger.warn("image_fallback_used: provider={} reason=UNKNOWN prompt={}", provider, req.prompt)
            fallback.generate(req)
        }
    }
}

@Service
class ImageGenerationService(
    @Qualifier("fallbackImageGenerator")
    private val imageGenerator: ImageGenerator,
    private val providerConfig: ImageProviderConfig,
    private val openAiImageConfig: OpenAiImageConfig,
    private val geminiImageConfig: GeminiImageConfig,
) {
    fun generate(req: ImageGenerateRequest): ImageGenerateResponse {
        val prompt = req.prompt.trim()
        require(prompt.isNotBlank()) { "prompt must not be blank" }

        val normalizedProvider = normalizeProvider(req.provider)
        if (req.provider != null && normalizedProvider == null) {
            throw IllegalArgumentException("provider must be one of: openai, gemini, nano-banana")
        }

        return imageGenerator.generate(req.copy(prompt = prompt, provider = normalizedProvider))
    }

    fun providerStatus(): ImageProviderStatusResponse {
        val activeProvider = normalizeProvider(providerConfig.provider) ?: "openai"
        return ImageProviderStatusResponse(
            activeProvider = activeProvider,
            providers =
                listOf(
                    ImageProviderStatusItem(
                        name = "openai",
                        enabled = openAiImageConfig.enabled,
                        apiKeyConfigured = openAiImageConfig.apiKey.isNotBlank(),
                        model = openAiImageConfig.model,
                        baseUrl = openAiImageConfig.baseUrl,
                        selected = activeProvider == "openai",
                    ),
                    ImageProviderStatusItem(
                        name = "gemini",
                        enabled = geminiImageConfig.enabled,
                        apiKeyConfigured = geminiImageConfig.apiKey.isNotBlank(),
                        model = geminiImageConfig.model,
                        baseUrl = geminiImageConfig.baseUrl,
                        selected = activeProvider == "gemini",
                    ),
                ),
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiImageResponse(
    val data: List<OpenAiImageData> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiImageData(
    @JsonProperty("b64_json")
    val b64Json: String? = null,
    val url: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiContent(
    val parts: List<GeminiPart> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiPart(
    val text: String? = null,
    @JsonProperty("inlineData")
    val inlineData: GeminiInlineData? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GeminiInlineData(
    @JsonProperty("mimeType")
    val mimeType: String? = null,
    val data: String? = null,
)
