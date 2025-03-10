package syntax.backend.runways.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import syntax.backend.runways.dto.WeatherDataDTO
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class WeatherServiceImpl : WeatherService {

    @Value("\${api.key}")
    private lateinit var apiKey: String

    @Value("\${api.url}")
    private lateinit var apiUrl: String

    private val restTemplate = RestTemplate()

    override fun getWeather(nx: String, ny: String): WeatherDataDTO {
        val now: LocalDateTime = LocalDateTime.now()
        val nowHour = now.plusHours(0).withMinute(0).withSecond(0).withNano(0)
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        val formattedTime = nowHour.format(timeFormatter)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formattedDate = nowHour.format(dateFormatter)

        val uri = "$apiUrl?serviceKey=$apiKey&numOfRows=10&pageNo=1&dataType=JSON&base_date=$formattedDate&base_time=$formattedTime&nx=$nx&ny=$ny"

        // TODO : 기상청 API 해결 필요함
        val response: String = restTemplate.getForObject(uri, String::class.java) ?: return WeatherDataDTO("No data", "No data", "No data", "No data")

        return extractWeatherData(response)
    }

    private fun extractWeatherData(json: String): WeatherDataDTO {
        val objectMapper = jacksonObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(json)
        val items = rootNode["response"]["body"]["items"]["item"]
        val weatherData = mutableMapOf<String, String>()

        items.forEach { item ->
            when (item["category"].asText()) {
                "TMP" -> weatherData["temperature"] = "${item["fcstValue"].asDouble()}°C"
                "REH" -> weatherData["humidity"] = "${item["fcstValue"].asInt()}%"
                "PCP" -> weatherData["precipitation"] = item["fcstValue"].asText()
                "WSD" -> weatherData["windSpeed"] = "${item["fcstValue"].asDouble()}m/s"
            }
        }

        return WeatherDataDTO(
            temperature = weatherData["temperature"] ?: "No data",
            humidity = weatherData["humidity"] ?: "No data",
            precipitation = weatherData["precipitation"] ?: "No data",
            windSpeed = weatherData["windSpeed"] ?: "No data"
        )
    }
}