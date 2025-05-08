package syntax.backend.runways.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import syntax.backend.runways.dto.WeatherDataDTO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.pow

@Service
class WeatherServiceImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : WeatherService {

    @Value("\${api.key}")
    private lateinit var apiKey: String

    @Value("\${api.forecast.weather.url}")
    private lateinit var apiForecastUrl: String

    @Value("\${api.now.weather.url}")
    private lateinit var apiNowUrl: String

    private val restTemplate = RestTemplate()

    // Redis 캐시를 사용하여 날씨 데이터 저장 및 조회
    override fun getWeatherByCity(city: String, nx: Double, ny: Double): WeatherDataDTO {
        val cacheKey = "weather:city:$city"
        val cachedData = redisTemplate.opsForValue().get(cacheKey)
        if (cachedData != null) {
            return objectMapper.readValue(cachedData, WeatherDataDTO::class.java)
        }

        val weatherData = getNowWeather(nx, ny)
        redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(weatherData), 1, TimeUnit.HOURS)
        return weatherData
    }

    override fun getNowWeather(nx: Double, ny: Double): WeatherDataDTO {
        // WGS84 좌표를 격자 좌표로 변환
        val (nyInt, nxInt) = convertWGS84ToGrid(ny, nx) // ny가 위도, nx가 경도

        val now: LocalDateTime = LocalDateTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

        // 현재 시간 기준 요청
        var nowHour = now.plusHours(0).withMinute(0).withSecond(0).withNano(0)
        var formattedTime = nowHour.format(timeFormatter)
        var formattedDate = nowHour.format(dateFormatter)

        // 요청 uri
        var uri = "$apiNowUrl?serviceKey=$apiKey&numOfRows=1000&pageNo=1&dataType=JSON&base_date=$formattedDate&base_time=$formattedTime&nx=$nyInt&ny=$nxInt"

        var response: String = restTemplate.getForObject(uri, String::class.java) ?: return WeatherDataDTO("-", "-", "-", "-", "-")
        var weatherData = extractNowWeatherData(response)

        // 결측값 확인
        val isTemperatureInvalid = weatherData.temperature.toDoubleOrNull()?.let { it > 50.0 || it < -50.0 } ?: true
        val isHumidityInvalid = weatherData.humidity.toIntOrNull()?.let { it > 100 || it < 0 } ?: true
        val isPrecipitationInvalid = weatherData.precipitation.toDoubleOrNull()?.let { it < 0.0 } ?: true
        val isWindSpeedInvalid = weatherData.windSpeed.toDoubleOrNull()?.let { it < 0.0 } ?: true

        if (isTemperatureInvalid || isHumidityInvalid || isPrecipitationInvalid || isWindSpeedInvalid) {
            nowHour = now.minusHours(1).withMinute(0).withSecond(0).withNano(0)
            formattedTime = nowHour.format(timeFormatter)
            formattedDate = nowHour.format(dateFormatter)

            uri = "$apiNowUrl?serviceKey=$apiKey&numOfRows=1000&pageNo=1&dataType=JSON&base_date=$formattedDate&base_time=$formattedTime&nx=$nyInt&ny=$nxInt"
            response = restTemplate.getForObject(uri, String::class.java) ?: return WeatherDataDTO("-", "-", "-", "-", "-")
            weatherData = extractNowWeatherData(response)
        }

        return weatherData
    }

    // 예보 조회
    override fun getForecastWeather(nx: Double, ny: Double): WeatherDataDTO {
        // WGS84 좌표를 격자 좌표로 변환
        val (nyInt, nxInt) = convertWGS84ToGrid(ny, nx) // ny가 위도, nx가 경도

        val now: LocalDateTime = LocalDateTime.now()
        val nowHour = now.plusHours(0).withMinute(0).withSecond(0).withNano(0)
        val timeFormatter = DateTimeFormatter.ofPattern("HHmm")
        var formattedTime = nowHour.format(timeFormatter)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        var formattedDate = nowHour.format(dateFormatter)

        // 요청 uri
        var uri = "$apiForecastUrl?serviceKey=$apiKey&numOfRows=10&pageNo=1&dataType=JSON&base_date=$formattedDate&base_time=$formattedTime&nx=$nyInt&ny=$nxInt"

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
            uri = createForecastUri(formattedDate, formattedTime, nyInt, nxInt)
            response = restTemplate.getForObject(uri, String::class.java) ?: return WeatherDataDTO("-", "-", "-", "-","-")
        }
        return extractForecastWeatherData(response)
    }

    private fun createForecastUri(formattedDate: String, formattedTime: String, x: Int, y: Int): String {
        return "$apiForecastUrl?serviceKey=$apiKey&numOfRows=10&pageNo=1&dataType=JSON&base_date=$formattedDate&base_time=$formattedTime&nx=$x&ny=$y"
    }

    // 예보 날씨 파싱
    private fun extractForecastWeatherData(json: String): WeatherDataDTO {
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

    // 단기실황조회 데이터 파싱
    private fun extractNowWeatherData(json: String): WeatherDataDTO {
        val objectMapper = jacksonObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(json)

        var temperature = "-"
        var humidity = "-"
        var precipitation = "-"
        var windSpeed = "-"
        var pty = "-" // 강수 형태

        if (rootNode["response"]["header"]["resultCode"].asText() == "00") {
            val items = rootNode["response"]["body"]["items"]["item"]
            val weatherData = mutableMapOf<String, String>()

            items.forEach { item ->
                when (item["category"].asText()) {
                    "T1H" -> weatherData["temperature"] = item["obsrValue"].asText()
                    "REH" -> weatherData["humidity"] = item["obsrValue"].asText()
                    "RN1" -> weatherData["precipitation"] = item["obsrValue"].asText()
                    "WSD" -> weatherData["windSpeed"] = item["obsrValue"].asText()
                    "PTY" -> weatherData["pty"] = item["obsrValue"].asText()
                }
            }
            temperature = weatherData["temperature"] ?: temperature
            humidity = weatherData["humidity"] ?: humidity
            precipitation = weatherData["precipitation"] ?: precipitation
            windSpeed = weatherData["windSpeed"] ?: windSpeed
            pty = when (weatherData["pty"]) {
                "0" -> "맑음"
                "1" -> "비"
                "2" -> "비/눈"
                "3" -> "눈"
                "4" -> "소나기"
                else -> "-"
            }
        }

        return WeatherDataDTO(temperature, humidity, precipitation, windSpeed, pty)
    }

    // WGS84 좌표를 기상청 격자 좌표로 변환하는 메서드 추가
    private fun convertWGS84ToGrid(lat: Double, lng: Double): Pair<Int, Int> {
        val RE = 6371.00877 // 지구 반경(km)
        val GRID = 5.0 // 격자 간격(km)
        val SLAT1 = 30.0 // 투영 위도1(degree)
        val SLAT2 = 60.0 // 투영 위도2(degree)
        val OLON = 126.0 // 기준점 경도(degree)
        val OLAT = 38.0 // 기준점 위도(degree)
        val XO = 43 // 기준점 X좌표 (GRID)
        val YO = 136 // 기준점 Y좌표 (GRID)

        val DEGRAD = Math.PI / 180.0

        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        val sn = kotlin.math.tan(Math.PI * 0.25 + slat2 * 0.5) / kotlin.math.tan(Math.PI * 0.25 + slat1 * 0.5)
        val snLog = kotlin.math.ln(kotlin.math.cos(slat1) / kotlin.math.cos(slat2)) / kotlin.math.ln(sn)
        val sf = kotlin.math.tan(Math.PI * 0.25 + slat1 * 0.5).pow(snLog) * kotlin.math.cos(slat1) / snLog
        val ro = re * sf / kotlin.math.tan(Math.PI * 0.25 + olat * 0.5).pow(snLog)

        val ra = re * sf / kotlin.math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5).pow(snLog)
        var theta = lng * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= snLog

        val x = (ra * kotlin.math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * kotlin.math.cos(theta) + YO + 0.5).toInt()

        return Pair(x, y)
    }

}