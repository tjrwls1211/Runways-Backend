package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.RunningLog
import java.time.LocalDateTime
import java.util.UUID

interface RunningLogRepository : JpaRepository<RunningLog, UUID> {
    fun findByUserIdOrderByEndTimeDesc(userId: String, pageable: Pageable): Page<RunningLog>
    fun findByEndTimeBetween(startTime: LocalDateTime, endTime: LocalDateTime) : List<RunningLog>
    @Query("""
    SELECT rl 
    FROM RunningLog rl 
    WHERE rl.user.id = :userId 
      AND rl.course IS NOT NULL 
      AND rl.course.status != :deletedStatus
    ORDER BY rl.endTime DESC
    """)
    fun findValidRunningLogsByUserId(
        @Param("userId") userId: String,
        @Param("deletedStatus") deletedStatus: CourseStatus,
        pageable: Pageable
    ): List<RunningLog>
}
