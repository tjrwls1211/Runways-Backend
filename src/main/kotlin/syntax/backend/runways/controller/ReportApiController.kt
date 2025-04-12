package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runways.dto.RequestReportDTO
import syntax.backend.runways.entity.Report
import syntax.backend.runways.service.ReportApiService

@RestController
@RequestMapping("/api/report")
class ReportApiController(
    private val reportApiService: ReportApiService
) {
    @PostMapping("/insert")
    fun insert(@RequestBody requestReportDTO: RequestReportDTO) : ResponseEntity<Report> {
        val result = reportApiService.createReport(requestReportDTO)
        return ResponseEntity.ok(result)
    }
}