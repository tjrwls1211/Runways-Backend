package syntax.backend.runways.controller

import syntax.backend.runways.service.FcmService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/fcm")
class FcmController(private val fcmService: FcmService) {

    data class NotificationRequest(val token: String, val title: String, val body: String)

    @PostMapping("/send")
    fun sendNotification(@RequestBody request: NotificationRequest) {
        fcmService.sendNotification(request.token, request.title, request.body)
    }
}