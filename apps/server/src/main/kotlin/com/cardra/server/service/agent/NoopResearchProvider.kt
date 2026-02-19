package com.cardra.server.service.agent

import org.springframework.stereotype.Component

@Component
class NoopResearchProvider : ResearchProvider {
    override fun fetch(keyword: String): List<String> {
        return listOf(
            "$keyword 관련 실시간 이슈",
            "$keyword 관련 리스크 및 기회 요인"
        )
    }
}
