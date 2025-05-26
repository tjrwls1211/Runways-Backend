package syntax.backend.runways.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import syntax.backend.runways.entity.CourseSegmentMapping
import java.util.UUID

interface CourseSegmentMappingRepository : JpaRepository<CourseSegmentMapping, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CourseSegmentMapping c WHERE c.course.id = :courseId")
    fun deleteByCourseId(@Param("courseId") courseId: UUID)

    @Query("SELECT c.segmentGid FROM CourseSegmentMapping c WHERE c.course.id = :courseId")
    fun findSegmentGidsByCourseId(@Param("courseId") courseId: UUID): List<Int>



}