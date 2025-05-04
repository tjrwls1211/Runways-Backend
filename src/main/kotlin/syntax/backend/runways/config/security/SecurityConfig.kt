package syntax.backend.runways.config.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtRequestFilter: JwtRequestFilter, private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // csrf 비활성화
            .csrf { it.disable() }
            // 세션 비활성화
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // 접근 권한 설정
            .authorizeHttpRequests {
                it.requestMatchers("/","/oauth2/**", "/login/**","/api/user/validate").permitAll()
                it.requestMatchers("/api/user/update", "/api/user/duplicatecheck")
                    .hasAnyRole("ADMIN", "USER", "WITHDRAWAL", "GUEST")
                it.anyRequest().hasAnyRole("ADMIN", "USER")
            }
            // oauth 로그인 설정
            .oauth2Login {
                it.successHandler(oAuth2LoginSuccessHandler)
            }
            // jwt 필터 추가
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}