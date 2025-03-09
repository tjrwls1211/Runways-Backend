package syntax.backend.runways.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
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

    // TODO : 테스트 필요
    @GetMapping("/duplicate-check")
    fun checkNickname(@RequestParam nickname: String): ResponseEntity<Boolean> {
        val isDuplicate = userApiService.isNicknameDuplicate(nickname)
        return ResponseEntity.ok(isDuplicate)
    }

    // TODO : 테스트 필요
    // 사용자 정보 업데이트
    @PostMapping("/update")
    fun signUp(@RequestHeader("Authorization") token: String, @RequestBody userInfoDTO: UserInfoDTO): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        userApiService.updateUserInfo(jwtToken, userInfoDTO)

        return ResponseEntity.status(HttpStatus.OK).body("사용자 정보 수정 성공")
    }
}