package syntax.backend.runways.repository

import org.springframework.data.jpa.repository.JpaRepository
import syntax.backend.runways.entity.Report
import java.util.*

interface ReportApiRepository : JpaRepository<Report, UUID> {
}