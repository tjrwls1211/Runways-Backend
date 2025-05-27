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
        endTime: LocalDateTime,
        pageable: Pageable
    ): Page<RunningLog>

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

    @Query("SELECT SUM(rl.distance) FROM RunningLog rl WHERE rl.user.id = :userId AND rl.status = :status")
    fun sumDistanceByUserIdAndStatus(
        @Param("userId") userId: String,
        @Param("status") status: RunningLogStatus
    ): Double?

    @Query("SELECT SUM(rl.duration) FROM RunningLog rl WHERE rl.user.id = :userId AND rl.status = :status")
    fun sumDurationByUserIdAndStatus(
        @Param("userId") userId: String,
        @Param("status") status: RunningLogStatus
    ): Long?

    fun countByUserIdAndStatus(
        userId: String,
        status: RunningLogStatus
    ): Long

    @Query("""
        SELECT FUNCTION('DAYOFWEEK', rl.startTime) AS dayOfWeek,
           AVG(rl.distance) AS avgDistance,
           AVG(rl.duration) AS avgDuration
        FROM RunningLog rl
        WHERE rl.user.id = :userId AND rl.status = :status
        GROUP BY FUNCTION('DAYOFWEEK', rl.startTime)
    """)
    fun findAvgDistanceAndDurationByDayOfWeek(
        @Param("userId") userId: String,
        @Param("status") status: RunningLogStatus
    ): List<Array<Any>>

    @Query("SELECT MAX(rl.maxSpeed) FROM RunningLog rl WHERE rl.user.id = :userId AND rl.status = :status")
    fun findMaxSpeedByUserIdAndStatus(
        @Param("userId") userId: String,
        @Param("status") status: RunningLogStatus
    ): Float?

    // 특정 기간 동안의 일별 기록 조회
    @Query("""
        SELECT DATE(r.endTime) AS date, COUNT(r) AS count
        FROM RunningLog r
        WHERE r.user.id = :userId AND r.status = :status
        AND r.endTime BETWEEN :startDate AND :endDate
        GROUP BY DATE(r.endTime)
    """)
    fun findDailyCountsByUserIdAndStatusAndDateBetween(
        @Param("userId") userId: String,
        @Param("status") status: RunningLogStatus,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Array<Any>>
}