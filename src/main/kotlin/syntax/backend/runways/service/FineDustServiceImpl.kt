package syntax.backend.runways.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import syntax.backend.runways.dto.FineDustDataDTO

@Service
class FineDustServiceImpl(
    private val locationApiService: LocationApiServiceImpl
) : FineDustService {

    @Value("\${api.key}")
    private lateinit var apiKey: String

    @Value("\${api.finedust.url}")
    private lateinit var apiUrl: String

    private val restTemplate = RestTemplate()

    // 미세먼지 API 호출
    override fun getFineDustData(x:Double, y:Double): FineDustDataDTO {

        // 가까운 관측소 찾기
        val nearestLocation = locationApiService.getNearestLocation(x, y)
        val sidoData = nearestLocation?.sido ?: return FineDustDataDTO("-","-", "-")

        val sido = if (sidoData.startsWith("충청") || sidoData.startsWith("경상") || sidoData.startsWith("전라")) {
            sidoData.substring(0, 1) + sidoData.substring(2, 3)
        } else {
            sidoData.substring(0, 2)
        }
        // 요청 URI
        val uri = "$apiUrl?sidoName=$sido&pageNo=1&numOfRows=100&returnType=json&serviceKey=$apiKey&ver=1.0"

      // 관측소 요청
        val response: String = restTemplate.getForObject(uri, String::class.java) ?: return FineDustDataDTO("-","-", "-")

        val objectMapper = jacksonObjectMapper()
        val rootNode: JsonNode = objectMapper.readTree(response)

        val items = rootNode["response"]["body"]["items"]

        // 관측소 이름 추출
        val stationName = nearestLocation.daegioyem

        val stationNameSigungu = nearestLocation.sigungu

        // 해당 관측소의 미세먼지 값 추출
        for (item in items) {
            if (item["stationName"].asText() == stationName) {
                val pm10value = item["pm10Value"].asText()
                val pm25value = item["pm25Value"].asText()
                return FineDustDataDTO(stationNameSigungu,pm10value, pm25value)
            }
        }
        return FineDustDataDTO("-","-", "-")

    }
}