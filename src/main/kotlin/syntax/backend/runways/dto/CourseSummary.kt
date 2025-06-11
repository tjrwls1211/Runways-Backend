package syntax.backend.runways.dto

import java.util.*

data class CourseSummary(
    val id : UUID,
    val title : String,
    val sido : String?,
    val sigungu : String?,
    val distance: Float,
    val mapUrl: String,
    val tags : List<String>,
    val usageCount : Int,
)