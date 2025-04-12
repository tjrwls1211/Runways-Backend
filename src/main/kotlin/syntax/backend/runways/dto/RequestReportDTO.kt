package syntax.backend.runways.dto

import syntax.backend.runways.entity.ReportType

data class RequestReportDTO (
    val type : ReportType,
    val detailId : String,
    val content : String,
)
