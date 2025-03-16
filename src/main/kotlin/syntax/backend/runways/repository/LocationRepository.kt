package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Location

interface LocationRepository : JpaRepository<Location, String> {
}