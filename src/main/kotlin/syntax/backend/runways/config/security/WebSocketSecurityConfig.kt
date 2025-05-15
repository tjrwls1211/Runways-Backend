import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager

@Configuration
@EnableWebSocketSecurity
class WebSocketSecurityConfig {

    @Bean
    fun messageSecurityCustomizer(): (MessageMatcherDelegatingAuthorizationManager.Builder) -> Unit {
        return { messages ->
            messages
                .simpDestMatchers("/app/status/**").authenticated()
                .simpSubscribeDestMatchers("/topic/status/**").authenticated()
                .anyMessage().denyAll()
        }
    }
}
