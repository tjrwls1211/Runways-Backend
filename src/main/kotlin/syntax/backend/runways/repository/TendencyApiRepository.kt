package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Tendency
import syntax.backend.runways.entity.User
import java.util.UUID

interface TendencyApiRepository  : JpaRepository<Tendency, UUID> {
    fun findByUser(User: User): Tendency?
}