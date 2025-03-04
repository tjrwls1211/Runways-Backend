package syntax.backend.runity.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import syntax.backend.runity.dto.OAuthTokenRequestDTO
import syntax.backend.runity.entity.User
import syntax.backend.runity.service.UserApiServiceImpl

@RestController
@RequestMapping("/api/oauth2")
class OAuthController(private val userApiService: UserApiServiceImpl) {

    @PostMapping("/kakao")
    fun loginWithKakao(@RequestBody tokenRequest: OAuthTokenRequestDTO): ResponseEntity<User> {
        val userInfo = userApiService.getKakaoUserInfo(tokenRequest.accessToken)
        val user = userApiService.saveOrUpdateUser(userInfo)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/google")
    fun loginWithGoogle(@RequestBody tokenRequest: OAuthTokenRequestDTO): ResponseEntity<User> {
        val userInfo = userApiService.getGoogleUserInfo(tokenRequest.accessToken)
        val user = userApiService.saveOrUpdateUser(userInfo)
        return ResponseEntity.ok(user)
    }
}