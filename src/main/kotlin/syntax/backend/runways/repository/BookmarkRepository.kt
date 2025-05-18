package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import syntax.backend.runways.dto.CourseBookmarkCount
import syntax.backend.runways.entity.Bookmark
import java.util.UUID

interface BookmarkRepository : JpaRepository<Bookmark, Long> {
    fun existsByCourseIdAndUserId(courseId : UUID, userId : String) : Boolean
    fun findByCourseIdAndUserId(courseId: UUID, userId: String): Bookmark?
    fun deleteByCourseIdAndUserId(courseId: UUID, userId: String)
    @Query("SELECT b.course.id FROM Bookmark b WHERE b.user.id = :userId AND b.course.id IN :courseIds")
    fun findBookmarkedCourseIdsByUserIdAndCourseIds(
        @Param("userId") userId: String,
        @Param("courseIds") courseIds: List<UUID>
    ): List<UUID>
    @Query("SELECT b.course.id FROM Bookmark b WHERE b.user.id = :userId")
    fun findCourseIdsByUserId(@Param("userId") userId: String, pageable: Pageable): Page<UUID>
    @Query("SELECT b.course.id AS courseId, COUNT(b) AS bookmarkCount FROM Bookmark b WHERE b.course.id IN :courseIds GROUP BY b.course.id")
    fun countBookmarksByCourseIds(@Param("courseIds") courseIds: List<UUID>): List<CourseBookmarkCount>
}

