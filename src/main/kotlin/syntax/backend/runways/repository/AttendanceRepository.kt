package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Attendance
import java.time.LocalDate
import java.util.UUID

@Repository
interface AttendanceRepository : JpaRepository<Attendance, UUID> {
    fun findByUserIdAndDate(userId: String, date: LocalDate): Attendance?
}