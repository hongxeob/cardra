package com.cardra.server.service.agent

import com.cardra.server.dto.CardItem
import org.springframework.stereotype.Component

@Component
class MockAgentAdapter(
    private val researchProvider: ResearchProvider,
) : AgentAdapter {

    override fun composeCards(keyword: String): List<CardItem> {
        val sourceTs = "2026-02-19T00:00:00Z"
        val refs = researchProvider.fetch(keyword)

        return listOf(
            CardItem(
                title = "$keyword 지금 왜 중요한가?",
                body = "${refs[0]} 기준으로 보면 현재 이슈는 단발성 반응보다 누적된 시장 변수와 정책·공급·수요 신호가 동시에 작동하는 구간입니다. 특히 24~72시간 이슈를 넘어서 주간 추세로 봐야 과열 논란을 줄일 수 있습니다.",
                source = listOf("agent://research", "agent://analysis"),
                sourceAt = sourceTs
            ),
            CardItem(
                title = "$keyword, 체크할 포인트",
                body = "${refs[1]} 쪽은 가격 신호·정책 변동·플랫폼 노출 지표가 동시 변동합니다. 단기 리스크와 중기 확장성을 분리해 점검하면 오판을 줄일 수 있고, 반복 보도보다 실제 수치와 발언자 신뢰도를 함께 보정해 판단해야 안정적입니다.",
                source = listOf("agent://analysis", "agent://editor"),
                sourceAt = sourceTs
            ),
            CardItem(
                title = "$keyword 핵심 요약",
                body = "최종 판단은 출처 2곳 이상 교차 확인 루틴이 가장 안전합니다. 수치 갱신이 발생할 때는 타임스탬프를 함께 보존하고, 다음 리스크 점검에서 동일 지표를 기준점으로 다시 비교해 의사결정 품질과 일관성을 동시에 높여야 합니다.",
                source = listOf("agent://validation", "agent://research"),
                sourceAt = sourceTs
            )
        )
    }
}
