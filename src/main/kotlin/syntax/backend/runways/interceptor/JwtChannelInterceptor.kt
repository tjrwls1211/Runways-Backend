package syntax.backend.runways.interceptor

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import syntax.backend.runways.util.JwtUtil

@Component
class JwtChannelInterceptor(
    private val jwtUtil: JwtUtil
) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
        if (accessor?.command == StompCommand.CONNECT) {
            val token = accessor.getFirstNativeHeader("Authorization")?.removePrefix("Bearer ") ?: return message
            if (jwtUtil.validateToken(token)) {
                val auth = UsernamePasswordAuthenticationToken(jwtUtil.extractUsername(token), null, emptyList())
                accessor.user = auth
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        return message
    }
}
