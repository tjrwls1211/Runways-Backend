package syntax.backend.runways.dto

import java.time.LocalDateTime
import java.util.UUID

data class NotificationDTO(
    val id: UUID,
    val title: String,
    val content: String,
    val type: String,
    val read: Boolean,
    val createdAt: LocalDateTime,
    val courseId: UUID?
)