package syntax.backend.runways.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.dto.ResponseCommentDTO
import syntax.backend.runways.entity.Comment
import syntax.backend.runways.entity.CommentStatus
import syntax.backend.runways.repository.CommentApiRepository
import syntax.backend.runways.repository.CourseApiRepository
import java.util.*

@Service
class CommentApiServiceImpl (
    private val commentApiRepository: CommentApiRepository,
    private val courseApiRepository: CourseApiRepository,
    private val userApiService: UserApiService
) : CommentApiService {

    // 댓글 다 불러오기
    override fun getCommentList(courseId: UUID, pageable:Pageable): List<ResponseCommentDTO> {
        val commentData = commentApiRepository.findByPostId_IdOrderByCreatedAtDesc(courseId, pageable)
        return commentData.map { comment ->
            ResponseCommentDTO (
                id = comment.id,
                content = comment.content,
                author = comment.author.nickname?: "",
                createdAt = comment.createdAt,
                updatedAt = comment.updatedAt,
                parent = comment.parent?.id,
            )
        }
    }

    override fun insertComment(requestInsertCommentDTO: RequestInsertCommentDTO, token: String): String {
        val courseData = courseApiRepository.findById(requestInsertCommentDTO.courseId).orElse(null) ?: return "Course not found"
        val user = userApiService.getUserDataFromToken(token)
        val parent = requestInsertCommentDTO.parentId?.let { commentApiRepository.findById(it).orElse(null) }

        val newComment = Comment(
            content = requestInsertCommentDTO.content,
            author = user,
            postId = courseData,
            status = CommentStatus.PUBLIC,
            parent = parent,
        )

        commentApiRepository.save(newComment)

        return "댓글 작성 성공"
    }

    // 댓글 업데이트
    override fun updateComment(commentId: UUID, content: String, token: String): String {
        val commentData = commentApiRepository.findById(commentId).orElse(null) ?: return "Comment not found"
        val user = userApiService.getUserDataFromToken(token)

        if (commentData.author.id != user.id) {
            return "댓글 작성자가 아닙니다."
        }

        // 새로운 객체 생성 후 저장
        val updatedCourse = commentData.copy(content = content)
        commentApiRepository.save(updatedCourse)

        return "댓글 업데이트 성공"
    }

    // 댓글 삭제
    override fun deleteComment(commentId: UUID, token: String): String {
        val commentData = commentApiRepository.findById(commentId)
        if (commentData.isPresent) {
            val comment = commentData.get()
            val user = userApiService.getUserDataFromToken(token)
            if (comment.author.id != user.id) {
                return "댓글 작성자가 아닙니다."
            }
            comment.status = CommentStatus.DELETED
            commentApiRepository.save(comment)
            return "댓글 삭제 성공"
        } else {
            return "댓글을 찾을 수 없습니다."
        }
    }
}