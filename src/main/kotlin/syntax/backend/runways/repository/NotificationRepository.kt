package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Notification
import java.util.UUID

@Repository
interface NotificationRepository : JpaRepository<Notification,UUID> {
    fun findByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Page<Notification>
}