package syntax.backend.runways.controller

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.NotificationDTO
import syntax.backend.runways.dto.NotificationRequestDTO
import syntax.backend.runways.dto.PagedResponse
import syntax.backend.runways.service.ExpoPushNotificationService
import syntax.backend.runways.service.NotificationApiService
import java.util.UUID

@RestController
@RequestMapping("/api/notification")
class NotificationApiController(
    private val notificationApiService: NotificationApiService,
    private val expoPushNotificationService: ExpoPushNotificationService
) {

    @GetMapping("/list")
    fun getNotifications(
        @RequestHeader("Authorization") token: String,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int
    ): ResponseEntity<PagedResponse<NotificationDTO>> {
        val pageable = PageRequest.of(page, size)
        val jwtToken = token.substring(7)
        val notifications = notificationApiService.getNotifications(jwtToken, pageable)

        val pagedResponse = PagedResponse(
            content = notifications.content,
            totalPages = notifications.totalPages,
            totalElements = notifications.totalElements,
            currentPage = notifications.number,
            pageSize = notifications.size
        )

        return ResponseEntity.ok(pagedResponse)
    }

    @PatchMapping("/read")
    fun markNotificationAsRead(@RequestParam notificationId: UUID): ResponseEntity<String> {
        return if (notificationApiService.markAsRead(notificationId))
            ResponseEntity.ok("알림 읽음")
        else
            ResponseEntity.notFound().build()
    }

    @PostMapping("/send")
    fun sendNotification(@RequestBody notificationRequestDTO:NotificationRequestDTO): ResponseEntity<String> {
        val result = expoPushNotificationService.sendPushNotification(notificationRequestDTO.token, notificationRequestDTO.title ,notificationRequestDTO.message)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/delete/{notificationId}")
    fun deleteNotification(@PathVariable notificationId: UUID): ResponseEntity<String> {
        return if (notificationApiService.deleteNotification(notificationId))
            ResponseEntity.ok("알림 삭제 성공")
        else
            ResponseEntity.notFound().build()
    }
}