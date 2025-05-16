package syntax.backend.runways.dto

import java.time.LocalDateTime
import java.util.*

data class RequestRunningLogDTO(
    val courseId: UUID?,
    val distance: Float,
    val duration: Long,
    val avgSpeed: Float,
    val maxSpeed: Float,
    val position: String,
    val coordinate: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)