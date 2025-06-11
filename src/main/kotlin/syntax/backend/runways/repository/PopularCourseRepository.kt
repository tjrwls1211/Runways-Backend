package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.PopularCourse
import java.time.LocalDate
import java.util.UUID

@Repository
interface PopularCourseRepository : JpaRepository<PopularCourse, UUID> {
    fun findByDate(date: LocalDate): List<PopularCourse>
    fun findByDateAndCourseId(date: LocalDate, courseId: UUID): PopularCourse?
    fun findByCourseId(courseId: UUID): List<PopularCourse>
}