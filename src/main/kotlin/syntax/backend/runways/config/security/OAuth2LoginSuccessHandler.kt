package syntax.backend.runways.config.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import syntax.backend.runways.repository.UserApiRepository
import syntax.backend.runways.service.LogService
import syntax.backend.runways.util.JwtUtil
import java.io.IOException

@Component
class OAuth2LoginSuccessHandler(
    private val jwtUtil: JwtUtil,
    private val userApiRepository: UserApiRepository,
    private val logService: LogService
) : AuthenticationSuccessHandler {


    @Throws(IOException::class)
    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication) {
        val oAuth2User = authentication.principal as DefaultOAuth2User
        val authorities = oAuth2User.authorities
        val attributes = oAuth2User.attributes
        val registrationId = (authentication as OAuth2AuthenticationToken).authorizedClientRegistrationId

        // 사용자 ID 가져오기
        val userId = when (registrationId) {
            "google" -> attributes["sub"] as String
            "kakao" -> attributes["id"].toString()
            else -> throw IllegalArgumentException("Unsupported provider: $registrationId")
        }

        // JWT 토큰 생성
        val jwt = jwtUtil.generateToken(userId)

        // SecurityContextHolder에 인증 정보 설정
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(oAuth2User, null, authorities)

        // 사용자 정보 가져오기
        val user = userApiRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }

        // nickname과 gender가 null인지 확인
        val userStatus = if (user.nickname.isNullOrEmpty() || user.gender.isNullOrEmpty()) 1 else 0

        // 로그 저장
        val ip = request.remoteAddr
        val userAgent = request.getHeader("User-Agent")
        logService.saveLog(user, "OAUTH_LOGIN", ip, userAgent, "/oauth2/authorization/${registrationId}")

        // 응답 바디에 JWT 토큰 및 사용자 상태 추가
        response.writer.write("{\"token\": \"$jwt\", \"status\": $userStatus}")
        response.writer.flush()
    }
}