package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import syntax.backend.runways.entity.Report
import java.util.*

@Repository
interface ReportRepository : JpaRepository<Report, UUID> {
}