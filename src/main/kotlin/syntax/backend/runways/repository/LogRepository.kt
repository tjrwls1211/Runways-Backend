package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Log
import java.util.UUID

interface LogRepository : JpaRepository<Log, UUID> {

}