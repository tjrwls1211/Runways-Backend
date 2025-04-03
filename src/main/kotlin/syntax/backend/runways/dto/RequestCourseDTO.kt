package syntax.backend.runways.dto

data class RequestCourseDTO (
    val title: String,
    val maker: String,
    val distance: Float,
    val position: String,
    val coordinate: String,
    val mapUrl: String
)