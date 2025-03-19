package syntax.backend.runways.controller

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.NotificationDTO
import syntax.backend.runways.service.NotificationApiService
import java.util.UUID

@RestController
@RequestMapping("/api/notification")
class NotificationApiController(
    private val notificationApiService: NotificationApiService
) {

    @GetMapping("/list")
    fun getNotifications(
        @RequestHeader("Authorization") token: String,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): List<NotificationDTO> {
        val pageable = PageRequest.of(page, size)
        val jwtToken = token.substring(7)
        return notificationApiService.getNotifications(jwtToken, pageable).content
    }

    @PatchMapping("/read")
    fun markNotificationAsRead(@RequestParam notificationId: UUID): ResponseEntity<String> {
        return if (notificationApiService.markAsRead(notificationId))
            ResponseEntity.ok("알림 읽음")
        else
            ResponseEntity.notFound().build()
    }
}