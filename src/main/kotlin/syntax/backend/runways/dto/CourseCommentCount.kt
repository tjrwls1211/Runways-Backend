package syntax.backend.runways.dto

import java.util.UUID

data class CourseCommentCount(
    val courseId: UUID,
    val commentCount: Long
)