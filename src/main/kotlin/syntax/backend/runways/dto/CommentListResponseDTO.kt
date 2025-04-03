package syntax.backend.runways.dto

import org.springframework.data.domain.Page

data class CommentListResponseDTO(
    val parentComments: Page<ResponseCommentDTO>,
    val childComments: Page<ResponseCommentDTO>
)