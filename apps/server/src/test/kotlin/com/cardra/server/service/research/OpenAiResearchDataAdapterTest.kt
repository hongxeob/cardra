package com.cardra.server.service.research

import com.cardra.server.dto.ResearchRunRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder

class OpenAiResearchDataAdapterTest {
    @Test
    fun `throws schema error when adapter is disabled`() {
        val config =
            OpenAiResearchConfig().apply {
                enabled = false
                apiKey = "sk-test"
                model = "gpt-4.1-mini"
            }
        val adapter = OpenAiResearchDataAdapter(config, jacksonObjectMapper(), RestTemplateBuilder())

        assertThrows(ExternalResearchSchemaError::class.java) {
            adapter.fetch(ResearchRunRequest(keyword = "AI"), "trace-1")
        }
    }

    @Test
    fun `throws schema error when api key is missing`() {
        val config =
            OpenAiResearchConfig().apply {
                enabled = true
                apiKey = ""
                model = "gpt-4.1-mini"
            }
        val adapter = OpenAiResearchDataAdapter(config, jacksonObjectMapper(), RestTemplateBuilder())

        assertThrows(ExternalResearchSchemaError::class.java) {
            adapter.fetch(ResearchRunRequest(keyword = "AI"), "trace-1")
        }
    }
}
