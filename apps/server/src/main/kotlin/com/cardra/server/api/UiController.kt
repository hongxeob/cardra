package com.cardra.server.api

import com.cardra.server.dto.UiContractsResponse
import com.cardra.server.dto.UiRouteInfo
import com.cardra.server.dto.UiThemeResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/ui")
class UiController {
    @GetMapping("/theme")
    fun theme(): ResponseEntity<UiThemeResponse> {
        return ResponseEntity.ok(
            UiThemeResponse(
                mainColor = "#00A676",
                subColor = "#E0D0C1",
            ),
        )
    }

    @GetMapping("/contracts")
    fun contracts(): ResponseEntity<UiContractsResponse> {
        return ResponseEntity.ok(
            UiContractsResponse(
                theme =
                    UiThemeResponse(
                        mainColor = "#00A676",
                        subColor = "#E0D0C1",
                    ),
                routes =
                    listOf(
                        UiRouteInfo("Card Generate", "POST", "/api/v1/cards/generate", "키워드로 카드 생성"),
                        UiRouteInfo("Card Get", "GET", "/api/v1/cards/{id}", "카드 단건 조회"),
                        UiRouteInfo("Research Run", "POST", "/api/v1/research/run", "동기형 리서치"),
                        UiRouteInfo("Research Job Create", "POST", "/api/v1/research/jobs", "비동기 리서치 생성"),
                        UiRouteInfo("Research Job Status", "GET", "/api/v1/research/jobs/{jobId}", "리서치 작업 상태"),
                        UiRouteInfo("Research Job Result", "GET", "/api/v1/research/jobs/{jobId}/result", "리서치 결과 조회"),
                        UiRouteInfo("Research Job Cancel", "POST", "/api/v1/research/jobs/{jobId}/cancel", "리서치 작업 취소"),
                        UiRouteInfo("Image Provider Status", "GET", "/api/v1/images/providers/status", "이미지 제공자 상태"),
                        UiRouteInfo("Image Generate", "POST", "/api/v1/images/generate", "이미지 생성"),
                        UiRouteInfo("Recommend Keywords", "POST", "/api/v1/recommend/keywords", "추천 키워드"),
                        UiRouteInfo("Recommend Events", "POST", "/api/v1/recommend/events", "이벤트 수집"),
                    ),
            ),
        )
    }
}
