package syntax.backend.runways.dto

import java.util.UUID

data class UpdateCommentDTO (
    val commentId: UUID,
    val content: String,
    val imageUrl: String?,
)