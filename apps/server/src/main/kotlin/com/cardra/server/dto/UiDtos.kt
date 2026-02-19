package com.cardra.server.dto

data class UiThemeResponse(
    val mainColor: String,
    val subColor: String,
    val background: String = "#FFFFFF",
    val textPrimary: String = "#0F172A",
    val textSecondary: String = "#334155",
)

data class UiRouteInfo(
    val feature: String,
    val method: String,
    val path: String,
    val description: String,
)

data class UiContractsResponse(
    val theme: UiThemeResponse,
    val routes: List<UiRouteInfo>,
)
