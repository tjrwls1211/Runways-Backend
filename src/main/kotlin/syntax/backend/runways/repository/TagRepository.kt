package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Tag
import java.util.UUID

interface TagRepository : JpaRepository<Tag, UUID> {
    fun findByNameContainingIgnoreCase(name: String): List<Tag>
    fun findTop10ByOrderByUsageCountDesc(): List<Tag>
    fun findByName(name: String): Tag?
}