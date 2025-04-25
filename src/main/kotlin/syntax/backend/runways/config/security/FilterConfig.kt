package syntax.backend.runways.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.userdetails.UserDetailsService
import syntax.backend.runways.util.JwtUtil

@Configuration
class FilterConfig(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) {

    @Bean
    fun jwtRequestFilter(): JwtRequestFilter {
        return JwtRequestFilter(jwtUtil, userDetailsService)
    }
}
