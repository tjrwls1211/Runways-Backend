package syntax.backend.runways.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import syntax.backend.runways.interceptor.LogInterceptor

@Configuration
class WebConfig(private val logInterceptor: LogInterceptor) : WebMvcConfigurer {

    // CORS 설정
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*") // 모든 출처 허용
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH") // 허용할 HTTP 메서드
            .allowedHeaders("*")
    }

    // 로그 인터셉터
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(logInterceptor)
    }

}