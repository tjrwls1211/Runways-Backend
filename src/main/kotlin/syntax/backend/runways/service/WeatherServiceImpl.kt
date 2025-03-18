package syntax.backend.runways.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import syntax.backend.runways.dto.WeatherDataDTO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class WeatherServiceImpl : WeatherService {

    @Value("\${api.key}")
    private lateinit var apiKey: String

    @Value("\${api.weather.url}")
    private lateinit var apiUrl: String

    private val restTemplate = RestTemplate()

    // 기상청 API 호출
    override fun getWeather(nx: Double, ny: Double): WeatherDataDTO {
        val now: LocalDateTime = LocalDateTime.now()
        val nowHour = now.plusHours(0).withMinute(0).withSecond(0).withNano(0)
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        var formattedTime = nowHour.format(timeFormatter)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        var formattedDate = nowHour.format(dateFormatter)

        // nx, ny 소수점 버림 후 정수로 변환
        val nxInt = nx.toInt()
        val nyInt = ny.toInt()

        // 요청 uri
        var uri = "$apiUrl?serviceKey=$apiKey&numOfRows=10&pageNo=1&dataType=JSON&base_date=$formattedDate&base_time=$formattedTime&nx=$nxInt&ny=$nyInt"

        // 기상청 API 요청
        var response: String = restTemplate.getForObject(uri, String::class.java) ?: return WeatherDataDTO("-", "-", "-", "-","-")

        val objectMapper = jacksonObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(response)

        // resultCode 값을 출력하여 확인
        val resultCode = rootNode["response"]["header"]["resultCode"].asText()

        // 만약 API 데이터가 없을 경우 -> resultCode가 03일 때, basetime 1시간 전으로 수정 후 요청
        if (resultCode == "03") {
            // 날짜가 바뀔 때
             if (formattedTime == "0000") {
                formattedTime = "2200"
                formattedDate = now.minusDays(1).format(dateFormatter)
            }
            // 01:00 ~ 23:00 사이일 때
             else {
                val beforeHour = now.minusHours(2).withMinute(0).withSecond(0).withNano(0)
                formattedTime = beforeHour.format(timeFormatter)
            }
            uri = createUri(formattedDate, formattedTime, nxInt, nyInt)
            response = restTemplate.getForObject(uri, String::class.java) ?: return WeatherDataDTO("-", "-", "-", "-","-")
        }
        return extractWeatherData(response)
    }

    private fun createUri(formattedDate: String, formattedTime: String, x: Int, y: Int): String {
        return "$apiUrl?serviceKey=$apiKey&numOfRows=10&pageNo=1&dataType=JSON&base_date=$formattedDate&base_time=$formattedTime&nx=$x&ny=$y"
    }

    // 날씨 파싱
    private fun extractWeatherData(json: String): WeatherDataDTO {
        val objectMapper = jacksonObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(json)

        var temperature = "-"
        var humidity = "-"
        var precipitation = "-"
        var windSpeed = "-"
        var sky = "-"

        if (rootNode["response"]["header"]["resultCode"].asText() == "00") {
            val items = rootNode["response"]["body"]["items"]["item"]
            val weatherData = mutableMapOf<String, String>()

            items.forEach { item ->
                when (item["category"].asText()) {
                    "TMP" -> weatherData["temperature"] = "${item["fcstValue"].asDouble()}°C"
                    "REH" -> weatherData["humidity"] = "${item["fcstValue"].asInt()}%"
                    "PCP" -> weatherData["precipitation"] = item["fcstValue"].asText()
                    "WSD" -> weatherData["windSpeed"] = "${item["fcstValue"].asDouble()}m/s"
                    "SKY" -> weatherData["sky"] = item["fcstValue"].asText()
                }
            }
            temperature = weatherData["temperature"] ?: temperature
            humidity = weatherData["humidity"] ?: humidity
            precipitation = weatherData["precipitation"] ?: precipitation
            windSpeed = weatherData["windSpeed"] ?: windSpeed
            sky = when (weatherData["sky"]) {
                "1" -> "맑음"
                "3" -> "구름 많음"
                "4" -> "흐림"
                else -> "-"
            }
        }

        return WeatherDataDTO( temperature, humidity, precipitation, windSpeed, sky )
    }
}