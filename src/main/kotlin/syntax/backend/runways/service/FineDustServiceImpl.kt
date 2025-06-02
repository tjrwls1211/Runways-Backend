package syntax.backend.runways.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import syntax.backend.runways.dto.FineDustDataDTO
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit

@Service
class FineDustServiceImpl(
    private val locationApiService: LocationApiServiceImpl,
    private val redisTemplate: StringRedisTemplate
) : FineDustService {

    @Value("\${api.key}")
    private lateinit var apiKey: String

    @Value("\${api.finedust.url}")
    private lateinit var apiUrl: String

    private val restTemplate = RestTemplate()

    override fun getFineDustData(x: Double, y: Double): FineDustDataDTO {
        val nearestLocation = locationApiService.getNearestLocation(x, y)
        val sidoData = nearestLocation?.sido ?: return FineDustDataDTO("-", "-", "-")

        val sido = if (sidoData.startsWith("충청") || sidoData.startsWith("경상") || sidoData.startsWith("전라")) {
            sidoData.substring(0, 1) + sidoData.substring(2, 3)
        } else {
            sidoData.substring(0, 2)
        }

        val redisKey = "finedust:$sido"

        // Redis에서 캐시 조회
        val cachedData = redisTemplate.opsForValue().get(redisKey)
        if (cachedData != null) {
            val objectMapper = jacksonObjectMapper()
            return objectMapper.readValue(cachedData, FineDustDataDTO::class.java)
        }

        // 요청 URI
        val uri = "$apiUrl?sidoName=$sido&pageNo=1&numOfRows=100&returnType=json&serviceKey=$apiKey&ver=1.0"
        val response: String = restTemplate.getForObject(uri, String::class.java) ?: return FineDustDataDTO("-", "-", "-")

        val objectMapper = jacksonObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(response)
        val items = rootNode["response"]["body"]["items"]

        val stationName = nearestLocation.daegioyem
        val stationNameSigungu = nearestLocation.sigungu

        for (item in items) {
            if (item["stationName"].asText() == stationName) {
                val pm10value = item["pm10Value"].asText()
                val pm25value = item["pm25Value"].asText()

                // 값이 유효한 경우에만 Redis에 저장
                if (pm10value != "-" && pm25value != "-") {
                    val fineDustData = FineDustDataDTO(stationNameSigungu, pm10value, pm25value)
                    redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(fineDustData), 1, TimeUnit.HOURS)
                    return fineDustData
                }
            }
        }
        return FineDustDataDTO("-", "-", "-")
    }
}