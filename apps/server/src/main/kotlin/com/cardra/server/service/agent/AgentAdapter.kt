package com.cardra.server.service.agent

import com.cardra.server.dto.CardItem

interface AgentAdapter {
  fun composeCards(keyword: String): List<CardItem>
}

interface ResearchProvider {
  fun fetch(keyword: String): List<String>
}
