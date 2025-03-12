package syntax.backend.runways.config.security

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import syntax.backend.runways.interceptor.LogInterceptor

@Configuration
class WebConfig(private val logInterceptor: LogInterceptor) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(logInterceptor)
    }
}