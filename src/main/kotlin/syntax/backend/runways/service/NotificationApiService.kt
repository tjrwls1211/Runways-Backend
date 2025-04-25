package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.NotificationDTO
import syntax.backend.runways.entity.User
import java.util.*

interface NotificationApiService {
    fun getNotifications(token: String, pageable: Pageable): Page<NotificationDTO>
    fun markAsRead(notificationId: UUID): Boolean
    fun addNotification(title:String, content:String, user: User, type:String, courseId: UUID)
    fun deleteNotification(notificationId: UUID): Boolean
}