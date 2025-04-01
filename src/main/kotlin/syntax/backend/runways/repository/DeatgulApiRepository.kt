package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Deatgul
import java.util.*

interface DeatgulApiRepository : JpaRepository<Deatgul, UUID> {
}