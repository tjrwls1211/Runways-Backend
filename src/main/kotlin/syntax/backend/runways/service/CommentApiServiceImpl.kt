package syntax.backend.runways.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RequestInsertCommentDTO
import syntax.backend.runways.dto.ResponseCommentDTO
import syntax.backend.runways.dto.UpdateCommentDTO
import syntax.backend.runways.entity.Comment
import syntax.backend.runways.entity.CommentStatus
import syntax.backend.runways.entity.User
import syntax.backend.runways.exception.NotAuthorException
import syntax.backend.runways.repository.CommentApiRepository
import java.util.*

@Service
class CommentApiServiceImpl (
    private val commentApiRepository: CommentApiRepository,
    private val courseApiService : CourseApiService,
    private val userApiService: UserApiService,
    private val expoPushNotificationService: ExpoPushNotificationService,
    private val notificationApiService: NotificationApiService
) : CommentApiService {

    // 댓글 불러오기(답글 X)
    override fun getParentCommentList(courseId: UUID, pageable: Pageable): Page<ResponseCommentDTO> {
        val status = CommentStatus.PUBLIC
        val commentData = commentApiRepository.findByPostId_IdAndStatusOrderByCreatedAtDesc(courseId, status, pageable)
        val filteredComments = commentData
            .filter { it.parent == null }
            .map { comment ->
                val childCount = commentApiRepository.countByParent_IdAndStatus(comment.id, status)
                ResponseCommentDTO(
                    id = comment.id,
                    content = comment.content,
                    author = comment.author.nickname ?: "",
                    createdAt = comment.createdAt,
                    updatedAt = comment.updatedAt,
                    parent = comment.parent?.id,
                    childCount = childCount,
                    imageUrl = comment.imageUrl
                )
            }.toList()
        return PageImpl(filteredComments, pageable, commentData.totalElements)
    }

    // 댓글 불러오기(답글 O)
    override fun getChildCommentList(parentId: UUID, courseId: UUID, pageable: Pageable): Page<ResponseCommentDTO> {
        val status = CommentStatus.PUBLIC
        val commentData = commentApiRepository.findByPostId_IdAndStatusOrderByCreatedAtDesc(courseId, status, pageable)
        val filteredComments = commentData
            .filter { it.parent?.id == parentId }
            .map { comment ->
                val childCount = commentApiRepository.countByParent_IdAndStatus(comment.id, status)
                ResponseCommentDTO(
                    id = comment.id,
                    content = comment.content,
                    author = comment.author.nickname ?: "",
                    createdAt = comment.createdAt,
                    updatedAt = comment.updatedAt,
                    parent = comment.parent?.id,
                    childCount = childCount,
                    imageUrl = comment.imageUrl
                )
            }.toList()
        return PageImpl(filteredComments, pageable, commentData.totalElements)
    }

    // 댓글 입력
    override fun insertComment(requestInsertCommentDTO: RequestInsertCommentDTO, token: String): ResponseCommentDTO {
        val courseData = courseApiService.getCourseData(requestInsertCommentDTO.courseId)
        val user = userApiService.getUserDataFromToken(token)
        val parent = requestInsertCommentDTO.parentId?.let { commentApiRepository.findById(it).orElse(null) }

        val newComment = Comment(
            content = requestInsertCommentDTO.content,
            author = user,
            postId = courseData,
            status = CommentStatus.PUBLIC,
            parent = parent,
            imageUrl = requestInsertCommentDTO.imageUrl
        )

        // 댓글 저장
        commentApiRepository.save(newComment)

        // 푸시 알림 전송
        val title = "새로운 댓글이 등록됐어요!"
        val type = "COMMENT"

        val expoPushToken: String
        val message: String
        val recipient: User

        if (parent == null && courseData.maker.id != user.id) {
            expoPushToken = courseData.maker.device ?: throw EntityNotFoundException("디바이스 토큰을 찾을 수 없습니다.")
            message = "${user.nickname}님이 코스에 댓글을 남겼어요 : ${requestInsertCommentDTO.content}"
            recipient = courseData.maker
        } else if (parent != null && parent.author.id != user.id) {
            expoPushToken = parent.author.device ?: throw EntityNotFoundException("디바이스 토큰을 찾을 수 없습니다.")
            message = "${user.nickname}님이 댓글에 답글을 남겼어요 : ${requestInsertCommentDTO.content}"
            recipient = parent.author
        } else {
            return ResponseCommentDTO(
                id = newComment.id,
                content = newComment.content,
                author = newComment.author.nickname ?: "",
                createdAt = newComment.createdAt,
                updatedAt = newComment.updatedAt,
                parent = newComment.parent?.id,
                childCount = 0,
                imageUrl = newComment.imageUrl
            )
        }

        notificationApiService.addNotification(title, message, recipient, type)
        expoPushNotificationService.sendPushNotification(expoPushToken, title, message)

        // 작성한 댓글 반환
        return ResponseCommentDTO(
            id = newComment.id,
            content = newComment.content,
            author = newComment.author.nickname ?: "",
            createdAt = newComment.createdAt,
            updatedAt = newComment.updatedAt,
            parent = newComment.parent?.id,
            childCount = 0,
            imageUrl = newComment.imageUrl
        )
    }

    // 댓글 업데이트
    override fun updateComment(updateCommentDTO: UpdateCommentDTO, token: String): String {
        val commentData = commentApiRepository.findById(updateCommentDTO.commentId).orElse(null) ?: throw EntityNotFoundException("Comment not found")
        val user = userApiService.getUserDataFromToken(token)

        if (commentData.author.id != user.id) {
            throw NotAuthorException("댓글 작성자가 아닙니다.")
        }

        // 새로운 객체 생성 후 저장
        val updatedCourse = commentData.copy(
            content = updateCommentDTO.content,
            imageUrl = updateCommentDTO.imageUrl
        )
        commentApiRepository.save(updatedCourse)

        return "댓글 업데이트 성공"
    }

    // 댓글 삭제
    override fun deleteComment(commentId: UUID, token: String): String {
        val commentData = commentApiRepository.findById(commentId)
        val childComment = commentApiRepository.findByParent_Id(commentId)
        if (commentData.isPresent) {
            val comment = commentData.get()
            val user = userApiService.getUserDataFromToken(token)
            if (comment.author.id != user.id) {
                throw NotAuthorException("댓글 작성자가 아닙니다.")
            }
            comment.status = CommentStatus.DELETED
            commentApiRepository.save(comment)
            if (childComment.isNotEmpty()) {
                for (child in childComment) {
                    child.status = CommentStatus.DELETED
                    commentApiRepository.save(child)
                }
            }
            return "댓글 삭제 성공"
        } else {
            throw EntityNotFoundException("댓글을 찾을 수 없습니다.")
        }
    }
}