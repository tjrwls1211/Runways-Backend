package syntax.backend.runways.dto

// 1월 ~ 말일 통계 DTO
data class MonthlyRunningStatsDTO(
    val averageDistance: Double,          // 평균 거리
    val averageDuration: Double,          // 평균 시간
    val averageCount: Double,             // 평균 횟수
    val totalDistance: Double,            // 누적 거리
    val totalDuration: Double,            // 누적 시간
    val dailyCounts: List<Int>  // 하루에 뛴 횟수 리스트
)