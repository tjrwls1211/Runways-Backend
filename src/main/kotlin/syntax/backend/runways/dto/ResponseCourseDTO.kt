package syntax.backend.runways.dto

import syntax.backend.runways.entity.BookMark
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.Hits
import syntax.backend.runways.entity.User
import java.time.LocalDateTime
import java.util.*

data class ResponseCourseDTO (
    val id: UUID,
    val title: String,
    val maker: User,
    val bookmark: BookMark,
    val hits: Hits,
    val distance: Float,
    // TODO 여기 수정해야됨 Linestring
    val coordinate: String,
    val mapUrl: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val author : Boolean,
    val status : CourseStatus
)