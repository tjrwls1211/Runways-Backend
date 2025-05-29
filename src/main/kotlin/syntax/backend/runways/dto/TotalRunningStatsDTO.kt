package syntax.backend.runways.dto

data class TotalRunningStatsDTO(
    val totalDistance: Double,  // 총 거리
    val totalDuration: Double,  // 총 시간
    val totalCount: Int         // 총 횟수
)