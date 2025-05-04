package syntax.backend.runways.util

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

object SecurityUtil {
    fun getCurrentUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = authentication.principal
        if (principal is UserDetails) {
            return principal.username // UserDetails의 username 필드에 사용자 ID가 저장되어 있음
        }
        throw IllegalStateException("인증된 사용자가 없습니다.")
    }
}