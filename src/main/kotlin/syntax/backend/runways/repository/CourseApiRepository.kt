package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseStatus
import java.util.*

interface CourseApiRepository : JpaRepository<Course, UUID> {
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseTags ct LEFT JOIN FETCH ct.tag WHERE c.maker.id = :makerId AND c.status IN :statuses")
    fun findByMaker_IdAndStatusInWithTags(@Param("makerId") makerId: String, @Param("statuses") statuses: List<CourseStatus>, pageable: Pageable): Page<Course>

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.courseTags ct LEFT JOIN FETCH ct.tag WHERE c.id = :courseId")
    fun findByIdWithTags(@Param("courseId") courseId: UUID): Optional<Course>

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseTags ct LEFT JOIN FETCH ct.tag WHERE c.status = :status")
    fun findByStatus(@Param("status") status: CourseStatus, pageable: Pageable): Page<Course>

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseTags ct LEFT JOIN FETCH ct.tag WHERE c.title LIKE %:title% AND c.status = :status")
    fun findByTitleContainingAndStatus(@Param("title") title: String, @Param("status") status: CourseStatus, pageable: Pageable): Page<Course>
}