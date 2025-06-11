package syntax.backend.runways.interceptor

import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import syntax.backend.runways.service.LogService
import syntax.backend.runways.service.UserApiService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import syntax.backend.runways.config.security.CustomUserDetails

@Component
class LogInterceptor(
    private val logService: LogService,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ip = getClientIp(request)
        val token = request.getHeader("Authorization")?.substring(7)
        val user = SecurityContextHolder.getContext().authentication?.let { auth ->
            if (auth.isAuthenticated && auth.principal is CustomUserDetails) {
                (auth.principal as CustomUserDetails).getUser()
            } else null
        }
        val type = "REQUEST"
        val value = request.getHeader("User-Agent")
        val requestUrl = request.requestURI

        logService.saveLog(user, type, ip, value, requestUrl, token ?: "")
        return true
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val ip = request.getHeader("X-Forwarded-For")
        return if (ip == null || ip.isEmpty() || "unknown".equals(ip, ignoreCase = true)) {
            request.remoteAddr
        } else {
            ip.split(",").toTypedArray()[0]
        }
    }
}