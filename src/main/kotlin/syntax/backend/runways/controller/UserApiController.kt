package syntax.backend.runways.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.RequestUserInfoDTO
import syntax.backend.runways.dto.ResponseUserInfoDTO
import syntax.backend.runways.service.UserApiService
import syntax.backend.runways.util.JwtUtil

@RestController
@RequestMapping("/api/user")
class UserApiController(
    private val userApiService: UserApiService,
    private val jwtUtil: JwtUtil
) {

    // 사용자 정보 호출
    @PostMapping("/info")
    fun getUserInfo(@RequestHeader("Authorization") token: String): ResponseEntity<ResponseUserInfoDTO> {
        val jwtToken = token.substring(7)
        val userInfo = userApiService.getUserInfoFromToken(jwtToken)
        return ResponseEntity.ok(userInfo)
    }

    // TODO : 테스트 필요
    @GetMapping("/duplicate-check")
    fun checkNickname(@RequestParam nickname: String): ResponseEntity<Boolean> {
        val isDuplicate = userApiService.isNicknameDuplicate(nickname)
        return ResponseEntity.ok (isDuplicate)
    }

    // TODO : 테스트 필요
    // 사용자 정보 업데이트
    @PatchMapping("/update")
    fun signUp(@RequestHeader("Authorization") token: String, @RequestBody requestUserInfoDTO: RequestUserInfoDTO): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        userApiService.updateUserInfo(jwtToken, requestUserInfoDTO)

        return ResponseEntity.status(HttpStatus.OK).body("사용자 정보 수정 성공")
    }

    @GetMapping("/validate")
    fun validateUserInfo(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val isValid = jwtUtil.validateToken(jwtToken)
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.")
        }
        return ResponseEntity.ok("사용자 정보가 유효합니다.")
    }
}