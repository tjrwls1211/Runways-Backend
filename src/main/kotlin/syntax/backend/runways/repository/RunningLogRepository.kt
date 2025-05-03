package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.RunningLog
import java.time.LocalDateTime
import java.util.UUID

interface RunningLogRepository : JpaRepository<RunningLog, UUID> {
    fun findByUserIdOrderByEndTimeDesc(userId: String, pageable: Pageable): Page<RunningLog>
    fun findByEndTimeBetween(startTime: LocalDateTime, endTime: LocalDateTime) : List<RunningLog>
}
