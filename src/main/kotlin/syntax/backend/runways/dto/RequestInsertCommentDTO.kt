package syntax.backend.runways.dto

import java.util.*

data class RequestInsertCommentDTO (
    val courseId: UUID,
    val content: String,
    val parentId: UUID? = null,
    val imageUrl: String? = null,
)
