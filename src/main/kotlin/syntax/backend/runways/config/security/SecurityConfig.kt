package syntax.backend.runways.config.security

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtRequestFilter: JwtRequestFilter,
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF 비활성화
            .csrf { it.disable() }

            // 세션 상태 없음
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // 요청 권한 설정
            .authorizeHttpRequests {
                it.requestMatchers("/", "/oauth2/**", "/login/**", "/api/user/validate", "/ws/**").permitAll()
                it.requestMatchers("/api/user/update", "/api/user/duplicatecheck")
                    .hasAnyRole("ADMIN", "USER", "WITHDRAWAL", "GUEST")
                it.anyRequest().hasAnyRole("ADMIN", "USER")
            }

            // OAuth2 로그인 성공 핸들러 설정
            .oauth2Login {
                it.successHandler(oAuth2LoginSuccessHandler)
            }

            // 예외 처리: REST API용 → 로그인 페이지가 아닌 401/403 반환
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
                it.accessDeniedHandler { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
                }
            }

            // JWT 필터 추가
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
