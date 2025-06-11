package syntax.backend.runways.dto

import java.time.LocalDateTime
import java.util.UUID

data class RecommendTagDTO(
    val id: UUID,
    val name: String,
    val score: Double,
    val lastUsed: LocalDateTime
)
