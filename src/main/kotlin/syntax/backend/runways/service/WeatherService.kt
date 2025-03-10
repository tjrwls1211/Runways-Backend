package syntax.backend.runways.service

import syntax.backend.runways.dto.WeatherDataDTO

interface WeatherService {
    fun getWeather(nx: String, ny: String): WeatherDataDTO
}