package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Tag
import java.util.UUID

interface TagApiRepository : JpaRepository<Tag, UUID> {
    // 대소문자 구분없이 조회
    fun findByNameContainingIgnoreCase(name: String): List<Tag>
}