package syntax.backend.runways.dto

import syntax.backend.runways.entity.Hits
import java.util.*

data class ResponseRecommendCourseDTO(
    val id : UUID,
    val title : String,
    val sido : String?,
    val sigungu : String?,
    val distance: Float,
    val hits: Hits?,
    val mapUrl: String,
    val tags : List<String>,
)