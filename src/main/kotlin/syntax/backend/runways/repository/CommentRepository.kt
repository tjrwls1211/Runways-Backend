package syntax.backend.runways.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Comment
import syntax.backend.runways.entity.CommentStatus
import java.util.*

interface CommentRepository : JpaRepository<Comment, UUID> {
    fun findByPostId_IdAndStatusOrderByCreatedAtDesc(postId: UUID, status: CommentStatus, pageable: Pageable): Page<Comment>
    fun countByPostId_IdAndStatus(postId: UUID, status: CommentStatus): Int
    fun countByParent_IdAndStatus(parentId: UUID, status: CommentStatus): Int
    fun findByParent_Id(parentId: UUID): List<Comment>
}