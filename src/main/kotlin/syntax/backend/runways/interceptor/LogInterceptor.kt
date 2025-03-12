package syntax.backend.runways.interceptor

import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import syntax.backend.runways.service.LogService
import syntax.backend.runways.service.UserApiService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import syntax.backend.runways.entity.User

@Component
class LogInterceptor(
    private val logService: LogService,
    private val userApiService: UserApiService,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val ip = getClientIp(request)
        val token = request.getHeader("Authorization")?.substring(7)
        val user = token?.let {
            userApiService.getUserDataFromToken(it)
        }
        val type = "REQUEST"
        val value = request.getHeader("User-Agent")

        logService.saveLog(user, type, ip, value)
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