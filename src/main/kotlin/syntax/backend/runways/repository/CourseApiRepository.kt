package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Course
import java.util.*

interface CourseApiRepository : JpaRepository<Course, UUID> {
    fun findByMaker_Id(makerId: String): List<Course>
}
