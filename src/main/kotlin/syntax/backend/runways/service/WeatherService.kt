package syntax.backend.runways.service

import syntax.backend.runways.dto.WeatherDataDTO

interface WeatherService {
    fun getWeatherByCity(city: String, nx: Double, ny: Double): WeatherDataDTO
    fun getNowWeather(nx: Double, ny: Double): WeatherDataDTO
    fun getForecastWeather(nx: Double, ny: Double): WeatherDataDTO
}