package syntax.backend.runways.service

import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.ResponseCommentDTO
import java.util.*

interface CommentApiService {
    fun getCommentList(courseId: UUID, pageable:Pageable): List<ResponseCommentDTO>
//    fun insertComment(courseId: UUID, content: String, token: String, parentId:UUID): String
    fun updateComment(commentId: UUID, content: String, token: String): String
    fun deleteComment(commentId: UUID, token: String): String
}