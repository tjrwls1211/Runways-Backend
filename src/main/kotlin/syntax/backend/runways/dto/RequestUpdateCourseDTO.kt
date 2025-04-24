package syntax.backend.runways.dto

import syntax.backend.runways.entity.CourseStatus
import java.util.*

data class RequestUpdateCourseDTO (
    val courseId : UUID,
    val title: String,
    val distance: Float,
    val position: String,
    val coordinate: String,
    val mapUrl: String,
    val status : CourseStatus,
)