package com.cardra.server.service.agent

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cardra.agent.external")
data class ExternalAgentConfig(
    val enabled: Boolean = false,
    val endpoint: String = "",
    val timeoutSeconds: Long = 6,
)
