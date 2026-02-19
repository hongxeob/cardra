package com.cardra.server.service.research

import com.cardra.server.dto.ResearchRunRequest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder

class ExternalResearchDataAdapterTest {
    @Test
    fun `throws schema error when adapter is disabled`() {
        val config =
            ExternalResearchConfig().apply {
                enabled = false
                endpoint = "https://example.com/research"
                timeoutSeconds = 5
            }
        val adapter =
            ExternalResearchDataAdapter(
                config,
                RestTemplateBuilder(),
            )

        assertThrows(ExternalResearchSchemaError::class.java) {
            adapter.fetch(ResearchRunRequest(keyword = "AI"), "trace-1")
        }
    }
}
