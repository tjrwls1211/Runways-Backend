package syntax.backend.runways.config.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import syntax.backend.runways.util.JwtUtil
import java.io.IOException

@Component
class JwtRequestFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService,
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val authorizationHeader = request.getHeader("Authorization")

        var username: String? = null
        var jwt: String? = null

        // JWT에서 사용자 정보 추출
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7)
            try {
                username = jwtUtil.extractUsername(jwt)
            } catch (e: ExpiredJwtException) {
                println("토큰이 만료되었습니다 : ${e.message}")
                response.status = HttpServletResponse.SC_UNAUTHORIZED // 401 Unauthorized 응답 설정
                return // 필터 체인을 중단
            } catch (e: JwtException) {
                println("유효하지 않은 JWT: ${e.message}")
                response.status = HttpServletResponse.SC_UNAUTHORIZED // 401 Unauthorized 응답 설정
                return // 필터 체인을 중단
            } catch (e: IllegalArgumentException) {
                println("잘못된 JWT: ${e.message}")
                response.status = HttpServletResponse.SC_UNAUTHORIZED // 401 Unauthorized 응답 설정
                return // 필터 체인을 중단
            }
        }

        // 사용자 인증 정보 설정
        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            val userDetails: UserDetails = userDetailsService.loadUserByUsername(username)
            if (jwt?.let { jwtUtil.validateToken(it) } == true) {
                val authenticationToken = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                authenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authenticationToken
            }
        }

        chain.doFilter(request, response)
    }
}
