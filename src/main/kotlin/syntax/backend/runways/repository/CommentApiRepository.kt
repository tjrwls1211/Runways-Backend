package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Comment
import java.util.*

interface CommentApiRepository : JpaRepository<Comment, UUID> {
    fun findByPostId_Id(courseId: String): List<Comment>
}