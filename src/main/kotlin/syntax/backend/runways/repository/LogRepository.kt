package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Log
import java.util.UUID

@Repository
interface LogRepository : JpaRepository<Log, UUID> {

}