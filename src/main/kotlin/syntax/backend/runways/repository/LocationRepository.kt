package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Location

@Repository
interface LocationRepository : JpaRepository<Location, String> {
}