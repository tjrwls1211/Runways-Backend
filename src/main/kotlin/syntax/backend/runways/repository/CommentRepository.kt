package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import syntax.backend.runways.dto.CourseCommentCount
import syntax.backend.runways.entity.Comment
import syntax.backend.runways.entity.CommentStatus
import java.util.*

interface CommentRepository : JpaRepository<Comment, UUID> {
    fun findByPost_IdAndStatusOrderByCreatedAtAsc(postId: UUID, status: CommentStatus, pageable: Pageable): Page<Comment>
    fun countByPost_IdAndStatus(postId: UUID, status: CommentStatus): Int
    fun countByParent_IdAndStatus(parentId: UUID, status: CommentStatus): Int
    fun findByParent_Id(parentId: UUID): List<Comment>
    /*
    // TODO : 정삳 작동 했다가 안됨
    @Query("SELECT c.post.id AS courseId, COUNT(c) AS commentCount FROM Comment c WHERE c.post.id IN :courseIds AND c.status = :status GROUP BY c.post.id")
    fun countCommentsByCourseIdsAndStatus(@Param("courseIds") courseIds: List<UUID>, @Param("status") status: CommentStatus): List<CourseCommentCount>
     */
    @Query("SELECT new syntax.backend.runways.dto.CourseCommentCount(c.post.id, COUNT(c)) FROM Comment c WHERE c.post.id IN :courseIds AND c.status = :status GROUP BY c.post.id")
    fun countCommentsByCourseIdsAndStatus(@Param("courseIds") courseIds: List<UUID>, @Param("status") status: CommentStatus): List<CourseCommentCount>}