package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.dto.ResponseCommentDTO
import syntax.backend.runways.dto.UpdateCommentDTO
import java.util.*

interface CommentApiService {
    fun getParentCommentList(courseId: UUID, pageable:Pageable, token : String): Page<ResponseCommentDTO>
    fun insertComment(requestInsertCommentDTO: RequestInsertCommentDTO, token: String): ResponseCommentDTO
    fun updateComment(updateCommentDTO: UpdateCommentDTO, token: String): String
    fun deleteComment(commentId: UUID, token: String): String
    fun getChildCommentList(parentId: UUID, courseId: UUID, pageable: Pageable, token : String): Page<ResponseCommentDTO>
}