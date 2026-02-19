package com.cardra.server.repository

import com.cardra.server.domain.CardEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CardRepository : JpaRepository<CardEntity, UUID>
