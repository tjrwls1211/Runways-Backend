package syntax.backend.runways.dto

import syntax.backend.runways.entity.CourseStatus

data class RequestCourseDTO (
    val title: String,
    val distance: Float,
    val position: String,
    val coordinate: String,
    val mapUrl: String,
    val status : CourseStatus,
)