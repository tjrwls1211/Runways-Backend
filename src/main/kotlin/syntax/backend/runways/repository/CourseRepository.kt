package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import syntax.backend.runways.entity.Course
import syntax.backend.runways.entity.CourseStatus
import java.util.*

interface CourseRepository : JpaRepository<Course, UUID> {
    @Query("""
        SELECT DISTINCT c FROM Course c 
        LEFT JOIN FETCH c.courseTags ct 
        LEFT JOIN FETCH ct.tag
        WHERE c.id = :courseId 
        AND c.status IN :statuses
    """)
    fun findCourseWithTagsByIdAndStatuses(@Param("courseId") courseId: UUID, @Param("statuses") statuses : List<CourseStatus>): Optional<Course>

    @Query("SELECT c.id FROM Course c WHERE c.status = :status")
    fun findCourseIdsByStatus(
        @Param("status") status: CourseStatus,
        pageable: Pageable
    ): Page<UUID>

    @Query("SELECT c.id FROM Course c WHERE c.title LIKE %:title% AND c.status = :status")
    fun findCourseIdsByTitleContainingAndStatus(
        @Param("title") title: String,
        @Param("status") status: CourseStatus,
        pageable: Pageable
    ): Page<UUID>

    @Query("SELECT c.id FROM Course c WHERE c.maker.id = :makerId AND c.status IN :statuses")
    fun findCourseIdsByMakerAndStatuses(
        @Param("makerId") makerId: String,
        @Param("statuses") statuses: List<CourseStatus>,
        pageable: Pageable
    ): Page<UUID>

    @Query("""
        SELECT DISTINCT c
        FROM Course c 
        LEFT JOIN FETCH c.maker m
        LEFT JOIN FETCH c.courseTags ct 
        LEFT JOIN FETCH ct.tag 
        WHERE c.id IN :ids
    """)
    fun findCoursesWithTagsByIds(@Param("ids") ids: List<UUID>): List<Course>

    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseTags ct LEFT JOIN FETCH ct.tag WHERE c.id IN :ids AND c.status = :status")
    fun findCoursesWithTagsByIdsAndStatus(
        @Param("ids") ids: List<UUID>,
        @Param("status") status: CourseStatus
    ): List<Course>

    // 코스 ID만 페이징
    @Query("""
        SELECT c.id
        FROM Course c
        JOIN c.courseTags ct
        WHERE ct.tag.id = :tagId 
        AND c.status = :status 
        AND c.maker.id != :userId
        ORDER BY c.usageCount DESC
    """)
    fun findCourseIdsByTagIdExcludingUser(
        @Param("tagId") tagId: UUID,
        @Param("status") status: CourseStatus,
        @Param("userId") userId: String,
        pageable: Pageable
    ): Page<UUID>

    // 최근 코스 조회
    @Query("""
        SELECT c.id
        FROM Course c
        WHERE c.status = :status
        ORDER BY c.createdAt DESC
    """)
    fun findTop10ByStatusOrderByCreatedAtDesc(status: CourseStatus): List<UUID>

    @Query("""
        SELECT id
        FROM courses
        WHERE difficulty IN (:difficulties)
            AND ST_DWithin(
              ST_Transform(position, 3857), 
                ST_Transform(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), 3857),
                :radius
            )
            AND status = 'PUBLIC'
        ORDER BY ST_Distance(
            ST_Transform(position, 3857),
            ST_Transform(ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), 3857)
        )
        LIMIT 10
    """, nativeQuery = true)
    fun findNearbyCourseIdsByDifficulty(
        @Param("lon") lon: Double,
        @Param("lat") lat: Double,
        @Param("difficulties") difficulties: List<String>,
        @Param("radius") radius: Double
    ): List<UUID>


}