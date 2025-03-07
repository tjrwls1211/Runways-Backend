package syntax.backend.runways.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import syntax.backend.runways.dto.UserInfoDTO
import syntax.backend.runways.entity.User
import syntax.backend.runways.service.UserApiService

@Controller
@RequestMapping("/api/user")
class UserApiController(private val userApiService: UserApiService) {

    // 사용자 정보 호출
    @PostMapping("/info")
    fun getUserInfo(@RequestHeader("Authorization") token: String): ResponseEntity<UserInfoDTO> {
        val jwtToken = token.substring(7)
        val userInfo = userApiService.getUserInfoFromToken(jwtToken)
        return ResponseEntity.ok(userInfo)
    }
}