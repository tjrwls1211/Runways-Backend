package syntax.backend.runways.dto

import java.time.LocalDateTime

data class RecommendTagDTO(
    val name: String,
    val score: Double,
    val lastUsed: LocalDateTime
)
