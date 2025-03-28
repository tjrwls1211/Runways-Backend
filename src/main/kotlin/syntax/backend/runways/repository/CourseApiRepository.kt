package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseStatus
import java.util.*

interface CourseApiRepository : JpaRepository<Course, UUID> {
    fun findByMaker_IdAndStatusIn(makerId: String, statuses: List<CourseStatus>): List<Course>
}