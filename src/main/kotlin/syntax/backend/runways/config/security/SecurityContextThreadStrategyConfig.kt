package syntax.backend.runways.config.security

import jakarta.annotation.PostConstruct
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class SecurityContextThreadStrategyConfig {

    // 스프링 컨텍스트 초기화 후 실행됨
    @PostConstruct
    fun init() {
        // SecurityContext를 스레드 간 상속 가능하도록 설정
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }
}
