package syntax.backend.runways.dto

import com.fasterxml.jackson.databind.node.ObjectNode
import syntax.backend.runways.entity.Bookmark
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.Hits
import syntax.backend.runways.entity.Tag
import syntax.backend.runways.entity.User
import java.time.LocalDateTime
import java.util.*

data class ResponseCourseDTO (
    val id: UUID,
    val title: String,
    val maker: User,
    val bookmark: Bookmark,
    val hits: Hits?,
    val distance: Float,
    val position: ObjectNode?,
    val coordinate: ObjectNode?,
    val mapUrl: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val author : Boolean,
    val status : CourseStatus,
    val tag : List<Tag>,
    val sido : String?,
    val sigungu : String?,
    val commentCount : Long,
)