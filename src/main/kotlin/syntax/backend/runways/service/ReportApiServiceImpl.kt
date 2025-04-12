package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RequestReportDTO
import syntax.backend.runways.entity.Report
import syntax.backend.runways.entity.ReportType
import syntax.backend.runways.repository.ReportApiRepository
import java.util.*

@Service
class ReportApiServiceImpl(
    private val reportApiRepository: ReportApiRepository
) : ReportApiService {


    override fun createReport(requestReportDTO: RequestReportDTO): Report {
        // ID 검사 로직
        if ((requestReportDTO.type == ReportType.COURSE || requestReportDTO.type == ReportType.COMMENT) && !isValidUuid(requestReportDTO.detailId)) {
            throw IllegalArgumentException("Invalid UUID format for detailId")
        }

        val report = Report(
            type = requestReportDTO.type,
            detailId = requestReportDTO.detailId,
            content = requestReportDTO.content
        )
        return reportApiRepository.save(report)
    }

    private fun isValidUuid(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}