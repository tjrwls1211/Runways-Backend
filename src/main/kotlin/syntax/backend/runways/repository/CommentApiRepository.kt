package syntax.backend.runways.repository

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Comment
import java.util.*

interface CommentApiRepository : JpaRepository<Comment, UUID> {
    fun findByPostId_IdOrderByCreatedAtDesc(courseId: UUID, pageable: Pageable): List<Comment>
}