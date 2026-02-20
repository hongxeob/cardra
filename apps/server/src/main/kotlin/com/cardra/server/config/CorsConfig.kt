package com.cardra.server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    private val corsProperties: CorsProperties,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        val allowedOrigins = corsProperties.allowedOrigins.map { it.trim() }.filter { it.isNotBlank() }
        val allowedOriginPatterns = corsProperties.allowedOriginPatterns.map { it.trim() }.filter { it.isNotBlank() }
        val effectiveAllowedOrigins =
            if (allowedOrigins.isEmpty() && allowedOriginPatterns.isEmpty()) {
                DEFAULT_LOCAL_ALLOWED_ORIGINS
            } else {
                allowedOrigins
            }

        val mapping =
            registry
                .addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600)

        if (effectiveAllowedOrigins.isNotEmpty()) {
            mapping.allowedOrigins(*effectiveAllowedOrigins.toTypedArray())
        }
        if (allowedOriginPatterns.isNotEmpty()) {
            mapping.allowedOriginPatterns(*allowedOriginPatterns.toTypedArray())
        }
    }

    companion object {
        private val DEFAULT_LOCAL_ALLOWED_ORIGINS =
            listOf(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:5173",
                "http://127.0.0.1:5173",
            )
    }
}
