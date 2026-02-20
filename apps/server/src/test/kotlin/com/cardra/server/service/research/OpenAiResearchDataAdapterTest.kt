package com.cardra.server.service.research

import com.cardra.server.dto.ResearchRunRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
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

    @Test
    fun `config has default webSearchTimeoutSeconds of 20`() {
        val config = OpenAiResearchConfig()
        assertEquals(20L, config.webSearchTimeoutSeconds)
    }

    @Test
    fun `throws schema error when web search returns empty text`() {
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
    fun `OpenAiResponsesResponse extracts text from message output`() {
        val mapper = jacksonObjectMapper()
        val json =
            """
            {
              "output": [
                {"type": "web_search_call", "content": []},
                {"type": "message", "content": [
                  {"type": "output_text", "text": "검색 결과 텍스트"}
                ]}
              ]
            }
            """.trimIndent()
        val response = mapper.readValue(json, OpenAiResponsesResponse::class.java)
        val text =
            response.output
                .filter { it.type == "message" }
                .flatMap { it.content }
                .filter { it.type == "output_text" }
                .mapNotNull { it.text }
                .joinToString("\n")
        assertEquals("검색 결과 텍스트", text)
    }
}
