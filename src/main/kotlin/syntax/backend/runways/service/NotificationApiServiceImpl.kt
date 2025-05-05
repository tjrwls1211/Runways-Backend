package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.NotificationDTO
import syntax.backend.runways.entity.Notification
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.NotificationRepository
import syntax.backend.runways.util.JwtUtil
import java.util.*

@Service
class NotificationApiServiceImpl(
    private val jwtUtil: JwtUtil,
    private val notificationRepository: NotificationRepository
) : NotificationApiService {

    // 알림 불러오기
    override fun getNotifications(userId: String, pageable: Pageable): Page<NotificationDTO> {
        val notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        val notificationDTOs = notifications.content.map { notification ->
            NotificationDTO(
                id = notification.id,
                title = notification.title,
                content = notification.content,
                type = notification.type,
                read = notification.read,
                createdAt = notification.createdAt,
                courseId = notification.courseId,
            )
        }
        return PageImpl(notificationDTOs, pageable, notifications.totalElements)
    }

    // 알림 읽음 처리
    override fun markAsRead(notificationId: UUID): Boolean {
        val notification = notificationRepository.findById(notificationId)
        if (notification.isPresent) {
            val existingNotification = notification.get()
            existingNotification.read = true
            notificationRepository.save(existingNotification)
            return true
        }
        return false
    }

    // 알림 추가
    override fun addNotification(title:String, content:String, user: User, type:String, courseId: UUID) {
        val notification = Notification(
            title = title,
            content = content,
            type = type,
            read = false,
            user = user,
            courseId = courseId
        )
        notificationRepository.save(notification)
    }

    // 알림 삭제
    override fun deleteNotification(notificationId: UUID): Boolean {
        val notification = notificationRepository.findById(notificationId)
        if (notification.isPresent) {
            notificationRepository.delete(notification.get())
            return true
        } else {
            return false
        }
    }
}