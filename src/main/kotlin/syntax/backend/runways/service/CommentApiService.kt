package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.dto.ResponseCommentDTO
import syntax.backend.runways.dto.UpdateCommentDTO
import java.util.*

interface CommentApiService {
    fun getParentCommentList(courseId: UUID, pageable:Pageable, userId : String): Page<ResponseCommentDTO>
    fun insertComment(requestInsertCommentDTO: RequestInsertCommentDTO, userId: String): ResponseCommentDTO
    fun updateComment(updateCommentDTO: UpdateCommentDTO, userId: String): String
    fun deleteComment(commentId: UUID, userId: String): String
    fun getChildCommentList(parentId: UUID, courseId: UUID, pageable: Pageable, userId : String): Page<ResponseCommentDTO>
}