package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.NotificationDTO
import syntax.backend.runways.entity.Notification
import syntax.backend.runways.repository.NotificationApiRepository
import syntax.backend.runways.util.JwtUtil
import java.util.*

@Service
class NotificationApiServiceImpl(
    private val jwtUtil: JwtUtil,
    private val notificationApiRepository: NotificationApiRepository
) : NotificationApiService {

    override fun getNotifications(token: String, pageable: Pageable): Page<NotificationDTO> {
        val userId = jwtUtil.extractUsername(token)
        val notifications = notificationApiRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        val notificationDTOs = notifications.content.map { notification ->
            NotificationDTO(
                id = notification.id,
                title = notification.title,
                content = notification.content,
                type = notification.type,
                read = notification.read,
                createdAt = notification.createdAt
            )
        }
        return PageImpl(notificationDTOs, pageable, notifications.totalElements)
    }

    override fun markAsRead(notificationId: UUID): Boolean {
        val notification = notificationApiRepository.findById(notificationId)
        if (notification.isPresent) {
            val existingNotification = notification.get()
            existingNotification.read = true
            notificationApiRepository.save(existingNotification)
            return true
        }
        return false
    }
}