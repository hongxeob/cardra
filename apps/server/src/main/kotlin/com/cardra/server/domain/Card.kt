package com.cardra.server.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "cards")
class CardEntity(
  @Id
  @GeneratedValue
  var id: UUID? = null,
  @Column(nullable = false)
  var keyword: String = "",
  @Column(columnDefinition = "TEXT")
  var content: String = "",
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  var status: CardStatus = CardStatus.COMPLETED,
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  var createdAt: Instant? = null,
  @UpdateTimestamp
  @Column(nullable = false)
  var updatedAt: Instant? = null,
  @Column(nullable = false)
  var sourceCount: Int = 0,
)

enum class CardStatus {
  PROCESSING,
  COMPLETED,
  FAILED,
}
