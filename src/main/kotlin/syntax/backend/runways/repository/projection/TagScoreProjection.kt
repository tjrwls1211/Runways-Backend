package syntax.backend.runways.repository.projection

import java.time.LocalDateTime
import java.util.UUID

interface TagScoreProjection {
    fun getId(): UUID
    fun getName(): String
    fun getScore(): Double
    fun getLastUsed(): LocalDateTime
}
