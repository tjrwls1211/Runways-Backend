package syntax.backend.runways.service

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

@Service
class ExpoPushNotificationService {

    private val expoPushUrl = "https://exp.host/--/api/v2/push/send"

    fun sendPushNotification(expoPushToken: String, title:String, message: String): String {
        val restTemplate = RestTemplate()

        var cleanedDeviceId = expoPushToken
        if (cleanedDeviceId.contains("{") || cleanedDeviceId.contains("}")) {
            cleanedDeviceId = cleanedDeviceId.replace("{", "[").replace("}", "]")
        }

        // Expo 푸시 알림 데이터 구성
        val requestBody = mapOf(
            "to" to cleanedDeviceId,  // Expo Go에서 받은 푸시 토큰
            "title" to title,  // 알림 제목
            "body" to message  // 알림 내용
        )

        // HTTP 헤더 구성
        val headers = HttpHeaders()
        headers.set("Accept", "application/json")
        headers.set("Content-Type", "application/json")

        // HTTP 엔티티 구성
        val entity = HttpEntity(requestBody, headers)

        // Expo 서버로 알림 전송
        val response: ResponseEntity<String> = restTemplate.exchange(
            expoPushUrl,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        return response.body ?: "알림 전송 실패"
    }
}
