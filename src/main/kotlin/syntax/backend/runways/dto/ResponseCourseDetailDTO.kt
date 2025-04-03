package syntax.backend.runways.dto

import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.Hits
import syntax.backend.runways.entity.User
import java.time.LocalDateTime
import java.util.*

data class ResponseCourseDetailDTO (
    val id: UUID,
    val title: String,
    val maker: User,
    val bookmark: Boolean,
    val hits: Hits,
    val distance: Float,
    val position: String?,
    val coordinate: String?,
    val mapUrl: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val author : Boolean,
    val status : CourseStatus,
    val tag : List<String>,
    val sido : String?,
    val sigungu : String?,
    val commentCount : Long,
)
