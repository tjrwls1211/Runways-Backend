package syntax.backend.runways.dto

data class ResponseRecommendCourseDTO (
    val title : String,
    val item : List<CourseSummary>
)