package syntax.backend.runways.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.CourseTag
import java.util.UUID

@Repository
interface CourseTagRepository : JpaRepository<CourseTag, UUID> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CourseTag ct WHERE ct.course.id = :courseId AND ct.tag.id IN :tagIds")
    fun deleteAllByCourseIdAndTagIdIn(@Param("courseId") courseId: UUID, @Param("tagIds") tagIds: List<UUID>)
}