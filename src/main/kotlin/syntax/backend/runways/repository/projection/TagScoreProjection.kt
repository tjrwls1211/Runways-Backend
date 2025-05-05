package syntax.backend.runways.repository.projection

import java.time.LocalDateTime

interface TagScoreProjection {
    fun getName(): String
    fun getScore(): Double
    fun getLastUsed(): LocalDateTime
}
