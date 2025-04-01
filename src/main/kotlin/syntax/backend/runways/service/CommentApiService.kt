package syntax.backend.runways.service

import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.dto.ResponseCommentDTO
import java.util.*

interface CommentApiService {
    fun getCommentList(courseId: UUID, pageable:Pageable): List<ResponseCommentDTO>
    fun insertComment(requestInsertCommentDTO: RequestInsertCommentDTO, token: String): String
    fun updateComment(commentId: UUID, content: String, token: String): String
    fun deleteComment(commentId: UUID, token: String): String
}