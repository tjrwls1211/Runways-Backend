package syntax.backend.runways.dto

import syntax.backend.runways.entity.CourseStatus
import java.util.UUID

data class RequestCourseDTO (
    val title: String,
    val distance: Float,
    val position: String,
    val coordinate: String,
    val mapUrl: String,
    val status : CourseStatus,
    val tag : List<UUID>,
    var sido : String,
    var sigungu : String
)