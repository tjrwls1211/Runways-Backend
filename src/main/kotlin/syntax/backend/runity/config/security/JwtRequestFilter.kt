package syntax.backend.runity.config.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import syntax.backend.runity.util.JwtUtil

class JwtRequestFilter {
    private val jwtUtil = JwtUtil()

//    fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
//        val authorizationHeader = request.getHeader("Authorization")
//        var username: String? = null
//        var jwt: String? = null
//
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            jwt = authorizationHeader.substring(7)
//            username = jwtUtil.extractUsername(jwt)
//        }
//
//        if (username != null && SecurityContextHolder.getContext().authentication == null) {
//            val userDetails = userDetailsService.loadUserByUsername(username)
//
//            if (jwtUtil.validateToken(jwt, userDetails)) {
//                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
//                usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
//                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
//            }
//        }
//
//        chain.doFilter(request, response)
//    }
}