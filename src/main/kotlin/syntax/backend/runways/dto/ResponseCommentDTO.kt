package syntax.backend.runways.dto

import java.time.LocalDateTime
import java.util.UUID

data class ResponseCommentDTO (
    val id : UUID,
    val content : String,
    val createdAt : LocalDateTime,
    val updatedAt : LocalDateTime,
    val author : String,
    val parent : UUID?,
    val childCount : Long,
    val imageUrl : String?,
    val maker : Boolean
)