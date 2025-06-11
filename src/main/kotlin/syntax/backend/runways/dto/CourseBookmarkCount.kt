package syntax.backend.runways.dto

import java.util.UUID

data class CourseBookmarkCount(
    val courseId: UUID,
    val bookmarkCount: Long,
)