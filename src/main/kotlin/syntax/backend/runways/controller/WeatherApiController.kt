package syntax.backend.runways.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runways.dto.WeatherDataDTO
import syntax.backend.runways.service.WeatherService

@RestController
@RequestMapping("/api/weather")
class WeatherApiController(private val weatherService: WeatherService) {

    @GetMapping
    fun getWeather(@RequestParam nx: Double, @RequestParam ny: Double): ResponseEntity<WeatherDataDTO> {
        val weatherData : WeatherDataDTO = weatherService.getNowWeather(nx, ny)
        return ResponseEntity.ok(weatherData)
    }

}
