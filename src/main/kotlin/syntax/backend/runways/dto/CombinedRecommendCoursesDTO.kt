package syntax.backend.runways.dto

data class CombinedRecommendCoursesDTO(
    val recentCourse: ResponseRecommendCourseDTO,
    val popularCourse: ResponseRecommendCourseDTO
)