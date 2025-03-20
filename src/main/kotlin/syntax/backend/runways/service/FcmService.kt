package syntax.backend.runways.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.springframework.stereotype.Service

@Service
class FcmService {

    fun sendNotification(token: String, title: String, body: String): String {
        val message = Message.builder()
            .setToken(token)
            .putData("title", title)
            .putData("body", body)
            .build()

        return FirebaseMessaging.getInstance().send(message)
    }
}