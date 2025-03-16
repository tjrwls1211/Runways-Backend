package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.User
import java.util.*

interface CourseApiRepository : JpaRepository<Course, UUID> {
    fun findByMaker(id: User): List<Course>
}