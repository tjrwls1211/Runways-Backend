package syntax.backend.runways.dto

data class WeatherDataDTO(
    val temperature: String,
    val humidity: String,
    val precipitation: String,
    val windSpeed: String,
    val sky : String,
)