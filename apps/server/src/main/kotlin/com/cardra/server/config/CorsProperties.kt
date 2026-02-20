package com.cardra.server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "cardra.cors")
class CorsProperties {
    var allowedOrigins: List<String> = emptyList()
    var allowedOriginPatterns: List<String> = emptyList()
}
