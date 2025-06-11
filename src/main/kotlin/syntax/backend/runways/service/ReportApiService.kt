package syntax.backend.runways.service

import syntax.backend.runways.dto.RequestReportDTO
import syntax.backend.runways.entity.Report

interface ReportApiService {
    fun createReport(requestReportDTO: RequestReportDTO): Report
}