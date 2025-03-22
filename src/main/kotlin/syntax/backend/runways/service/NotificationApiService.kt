package syntax.backend.runways.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import syntax.backend.runways.dto.NotificationDTO
import java.util.*

interface NotificationApiService {
    fun getNotifications(token: String, pageable: Pageable): Page<NotificationDTO>
    fun markAsRead(notificationId: UUID): Boolean
}