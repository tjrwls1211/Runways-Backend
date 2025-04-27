package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.PopularCourse
import java.time.LocalDate
import java.util.UUID

interface PopularCourseRepository : JpaRepository<PopularCourse, UUID> {
    fun findByDate(date: LocalDate): List<PopularCourse>
}