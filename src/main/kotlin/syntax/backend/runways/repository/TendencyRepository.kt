package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Tendency
import syntax.backend.runways.entity.User
import java.util.UUID

@Repository
interface TendencyRepository  : JpaRepository<Tendency, UUID> {
    fun findByUser(user: User): Tendency?
}