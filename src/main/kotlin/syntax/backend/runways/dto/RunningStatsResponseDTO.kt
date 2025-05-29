package syntax.backend.runways.dto

data class RunningStatsResponseDTO(
    val monthlyStats: MonthlyRunningStatsDTO,  // 월별 통계
    val totalStats: TotalRunningStatsDTO      // 전체 누적 통계
)