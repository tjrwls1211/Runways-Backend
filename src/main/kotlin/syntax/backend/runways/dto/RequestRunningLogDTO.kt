package syntax.backend.runways.dto

import java.time.LocalDateTime
import java.util.*

data class RequestRunningLogDTO(
    val courseId: UUID?,
    val distance: Float,
    val duration: Long,
    val speed: Float,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)