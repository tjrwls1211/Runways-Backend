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
import syntax.backend.runways.repository.CommentRepository
import java.util.*

@Service
class CommentApiServiceImpl (
    private val commentRepository: CommentRepository,
    private val courseApiService : CourseApiService,
    private val userApiService: UserApiService,
    private val expoPushNotificationService: ExpoPushNotificationService,
    private val notificationApiService: NotificationApiService,
    private val experienceService : ExperienceService
) : CommentApiService {

    // 댓글 불러오기(답글 X)
    override fun getParentCommentList(courseId: UUID, pageable: Pageable, userId : String): Page<ResponseCommentDTO> {
        // 댓글 불러오기
        val status = CommentStatus.PUBLIC
        val commentData = commentRepository.findByPost_IdAndStatusOrderByCreatedAtAsc(courseId, status, pageable)
        val filteredComments = commentData
            .filter { it.parent == null }
            .map { comment ->
                val childCount = commentRepository.countByParent_IdAndStatus(comment.id, status)
                ResponseCommentDTO(
                    id = comment.id,
                    content = comment.content,
                    author = comment.author.nickname ?: "",
                    createdAt = comment.createdAt,
                    updatedAt = comment.updatedAt,
                    parent = comment.parent?.id,
                    childCount = childCount,
                    imageUrl = comment.imageUrl,
                    maker = comment.author.id == userId
                )
            }.toList()
        return PageImpl(filteredComments, pageable, commentData.totalElements)
    }

    // 댓글 불러오기(답글 O)
    override fun getChildCommentList(parentId: UUID, courseId: UUID, pageable: Pageable, userId : String): Page<ResponseCommentDTO> {
        // 답글 불러오기
        val status = CommentStatus.PUBLIC
        val commentData = commentRepository.findByPost_IdAndStatusOrderByCreatedAtAsc(courseId, status, pageable)
        val filteredComments = commentData
            .filter { it.parent?.id == parentId }
            .map { comment ->
                val childCount = commentRepository.countByParent_IdAndStatus(comment.id, status)
                ResponseCommentDTO(
                    id = comment.id,
                    content = comment.content,
                    author = comment.author.nickname ?: "",
                    createdAt = comment.createdAt,
                    updatedAt = comment.updatedAt,
                    parent = comment.parent?.id,
                    childCount = childCount,
                    imageUrl = comment.imageUrl,
                    maker = comment.author.id == userId
                )
            }.toList()
        return PageImpl(filteredComments, pageable, commentData.totalElements)
    }

    // 댓글 입력
    override fun insertComment(requestInsertCommentDTO: RequestInsertCommentDTO, userId: String): ResponseCommentDTO {
        val courseData = courseApiService.getCourseData(requestInsertCommentDTO.courseId)
        val user = userApiService.getUserDataFromId(userId)
        val parent = requestInsertCommentDTO.parentId?.let { commentRepository.findById(it).orElse(null) }

        val newComment = Comment(
            content = requestInsertCommentDTO.content,
            author = user,
            post = courseData,
            status = CommentStatus.PUBLIC,
            parent = parent,
            imageUrl = requestInsertCommentDTO.imageUrl
        )

        // 경험치 추가: 부모 댓글이면 3, 대댓글이면 1
        val experiencePoints = if (parent == null) 3 else 1
        experienceService.addExperience(user, experiencePoints)

        // 댓글 저장
        commentRepository.save(newComment)

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
                imageUrl = newComment.imageUrl,
                maker = true
            )
        }

        notificationApiService.addNotification(title, message, recipient, type, courseData.id)
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
            imageUrl = newComment.imageUrl,
            maker = true
        )
    }

    // 댓글 업데이트
    override fun updateComment(updateCommentDTO: UpdateCommentDTO, userId: String): String {
        val commentData = commentRepository.findById(updateCommentDTO.commentId).orElse(null) ?: throw EntityNotFoundException("Comment not found")

        if (commentData.author.id != userId) {
            throw NotAuthorException("댓글 작성자가 아닙니다.")
        }

        // 새로운 객체 생성 후 저장
        val updatedCourse = commentData.copy(
            content = updateCommentDTO.content,
            imageUrl = updateCommentDTO.imageUrl
        )
        commentRepository.save(updatedCourse)

        return "댓글 업데이트 성공"
    }

    // 댓글 삭제
    override fun deleteComment(commentId: UUID, userId: String): String {
        val commentData = commentRepository.findById(commentId)
        val childComment = commentRepository.findByParent_Id(commentId)
        if (commentData.isPresent) {
            val comment = commentData.get()

            // 댓글 작성자와 요청한 사용자가 같은지 확인
            if (comment.author.id != userId) {
                throw NotAuthorException("댓글 작성자가 아닙니다.")
            }
            comment.status = CommentStatus.DELETED
            commentRepository.save(comment)
            if (childComment.isNotEmpty()) {
                for (child in childComment) {
                    child.status = CommentStatus.DELETED
                    commentRepository.save(child)
                }
            }
            return "댓글 삭제 성공"
        } else {
            throw EntityNotFoundException("댓글을 찾을 수 없습니다.")
        }
    }
}