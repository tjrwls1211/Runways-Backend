package syntax.backend.runways.interceptor

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import syntax.backend.runways.util.JwtUtil

@Component
class JwtChannelInterceptor(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        val authHeader = accessor.getFirstNativeHeader("Authorization")

        if (!authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ") && accessor.command == StompCommand.CONNECT) {
            val jwt = authHeader.removePrefix("Bearer ").trim()

            try {
                val username = jwtUtil.extractUsername(jwt)
                if (jwtUtil.validateToken(jwt)) {
                    val userDetails = userDetailsService.loadUserByUsername(username)
                    val auth = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

                    SecurityContextHolder.getContext().authentication = auth
                    accessor.user = auth

                    // 세션 속성에 사용자 ID 저장
                    accessor.sessionAttributes?.set("userId", userDetails.username)

                    println("[JWT] 인증 정보 설정 완료: ${auth.name} (${accessor.command})")
                }
            } catch (e: ExpiredJwtException) {
                println("[JWT] 토큰 만료: ${e.message}")
            } catch (e: JwtException) {
                println("[JWT] 유효하지 않은 토큰: ${e.message}")
            } catch (e: IllegalArgumentException) {
                println("[JWT] 토큰 파싱 실패: ${e.message}")
            }
        }

        return message
    }
}
