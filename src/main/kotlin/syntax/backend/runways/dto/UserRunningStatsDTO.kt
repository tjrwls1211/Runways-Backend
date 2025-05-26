package syntax.backend.runways.dto

data class UserRunningStatsDTO(
    val totalRunningDistance: Double,
    val totalRunningTime: Long,
    val totalWorkoutCount: Int,
    val averageDistance: Double,
    val averageDuration: Double,
    val averagePace: Double,   // 분/km
    val maxSpeed: Float
)
