package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.CourseTag
import java.util.UUID

interface CourseTagRepository : JpaRepository<CourseTag, UUID> {
    fun deleteAllByCourseIdAndTagIdIn(courseId: UUID, tagIds: Set<UUID>)
}