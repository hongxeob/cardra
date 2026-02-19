package com.cardra.server

import com.cardra.server.service.agent.ExternalAgentConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = [ExternalAgentConfig::class])
class CardraApplication

fun main(args: Array<String>) {
    runApplication<CardraApplication>(*args)
}
