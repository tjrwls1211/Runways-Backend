package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Tendency
import syntax.backend.runways.entity.User
import java.util.UUID

interface TendencyRepository  : JpaRepository<Tendency, UUID> {
    fun findByUser(user: User): Tendency?
}