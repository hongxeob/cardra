package com.cardra.server.service.agent

import com.cardra.server.dto.CardItem
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class FallbackAgentAdapter(
    private val primaryAgent: MockAgentAdapter,
) : AgentAdapter {
    override fun composeCards(keyword: String): List<CardItem> {
        return try {
            primaryAgent.composeCards(keyword)
        } catch (_: Exception) {
            val sourceTs = "${java.time.Instant.now()}"
            listOf(
                CardItem(
                    title = "$keyword 요약",
                    body =
                        "외부 에이전트 호출이 지연되었거나 실패했습니다. " +
                            "이 시간에는 정책·수치·콘텐츠 신호를 안정 모드로만 조합해 드리며, " +
                            "핵심은 같은 이슈라도 출처 2곳 이상을 교차 확인해 판단 편차를 줄이는 것입니다. " +
                            "가능한 한 다음 조회 타이밍까지는 신호가 겹치는 관측치 중심으로 유지하세요.",
                    source = listOf("agent://fallback"),
                    sourceAt = sourceTs,
                ),
                CardItem(
                    title = "$keyword 점검 포인트",
                    body =
                        "현재는 안전 모드입니다. 동일 키워드에 대한 최근 12~24시간 변화를 확인하고, " +
                            "정책 변경, 수요 신호, 플랫폼 지표를 단계별로 다시 점검해 리스크 반영 " +
                            "속도와 과열 가능성을 낮춰 재평가하는 절차를 권장합니다. 보수적으로 보면 추세 " +
                            "반전 징후를 먼저 분리해 보는 방식이 안정적입니다.",
                    source = listOf("agent://fallback", "agent://system"),
                    sourceAt = sourceTs,
                ),
                CardItem(
                    title = "$keyword 권장 액션",
                    body =
                        "재요청 전에는 보수적 판단 플로우를 유지하고, 동일 출처의 타임스탬프와 " +
                            "시계열 수치를 함께 기록해 동일한 결정을 반복 검증하세요. 다음 조회 시 재요청하면 " +
                            "최신 에이전트 결과가 반영되며, 기존 판단 근거의 근원성도 함께 비교해 점검 " +
                            "가능합니다.",
                    source = listOf("agent://fallback", "agent://safety"),
                    sourceAt = sourceTs,
                ),
            )
        }
    }
}
