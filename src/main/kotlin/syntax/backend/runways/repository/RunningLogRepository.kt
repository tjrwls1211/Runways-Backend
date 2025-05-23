package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import syntax.backend.runways.entity.CourseStatus
import syntax.backend.runways.entity.RunningLog
import syntax.backend.runways.entity.RunningLogStatus
import java.time.LocalDateTime
import java.util.UUID

interface RunningLogRepository : JpaRepository<RunningLog, UUID> {
    fun findByUserIdAndStatusAndEndTimeBetweenOrderByEndTimeDesc(
        userId: String,
        status: RunningLogStatus,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ): List<RunningLog>

    fun findByEndTimeBetween(startTime: LocalDateTime, endTime: LocalDateTime): List<RunningLog>

    @Query("""
        SELECT rl.course.id 
        FROM RunningLog rl 
        WHERE rl.user.id = :userId 
        AND rl.course.status != :status 
        ORDER BY rl.endTime DESC
    """)
    fun findTop5CourseIdsByUserIdAndCourseStatusNotOrderByEndTimeDesc(
        @Param("userId") userId: String,
        @Param("status") status: CourseStatus
    ): List<UUID>
}