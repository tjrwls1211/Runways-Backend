package syntax.backend.runways.controller

import syntax.backend.runways.service.FcmService
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.NotificationRequestDTO

@RestController
@RequestMapping("/api/fcm")
class FcmController(private val fcmService: FcmService) {

    @PostMapping("/send")
    fun sendNotification(@RequestBody request: NotificationRequestDTO) {
        fcmService.sendNotification(request.token, request.title, request.body)
    }
}