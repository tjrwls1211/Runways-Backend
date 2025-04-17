package syntax.backend.runways.service

import syntax.backend.runways.dto.WeatherDataDTO

interface WeatherService {
    fun getNowWeather(nx: Double, ny: Double): WeatherDataDTO
    fun getForecastWeather(nx: Double, ny: Double): WeatherDataDTO
}