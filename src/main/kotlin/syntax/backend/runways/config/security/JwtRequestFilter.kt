package syntax.backend.runways.config.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import syntax.backend.runways.util.JwtUtil
import java.io.IOException

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

        // Authorization 헤더에서 JWT 추출
        val authHeader = request.getHeader("Authorization")
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response)
            return
        }

        val jwt = authHeader.substring(7)
        val username = try {
            jwtUtil.extractUsername(jwt)
        } catch (e: ExpiredJwtException) {
            println("JWT 만료: ${e.message}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "토큰 만료됨")
            return
        } catch (e: JwtException) {
            println("JWT 파싱 오류: ${e.message}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 오류")
            return
        } catch (e: IllegalArgumentException) {
            println("JWT 구조 오류: ${e.message}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 파싱 실패")
            return
        }

        if (SecurityContextHolder.getContext().authentication == null) {
            val userDetails = userDetailsService.loadUserByUsername(username)
            val customUserDetails = userDetails as? CustomUserDetails
                ?: throw IllegalStateException("CustomUserDetails 타입 캐스팅 실패")

            if (jwtUtil.validateToken(jwt)) {
                val authentication = UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    null,
                    customUserDetails.authorities
                ).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }

                SecurityContextHolder.getContext().authentication = authentication
            } else {
                println("JWT 유효성 검사 실패")
            }

        } else {
            println("이미 인증 정보가 설정되어 있음: ${SecurityContextHolder.getContext().authentication.name}")
        }

        chain.doFilter(request, response)
    }
}
