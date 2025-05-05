package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Attendance
import syntax.backend.runways.entity.User
import java.time.LocalDate
import java.util.UUID

interface AttendanceRepository : JpaRepository<Attendance, UUID> {
    fun findByUserIdAndDate(userId: String, date: LocalDate): Attendance?
}