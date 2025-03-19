package syntax.backend.runways.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import syntax.backend.runways.dto.DeviceIdDTO
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

    // 카카오 리디렉트 URL
    @GetMapping("/kakao")
    fun kakaoLogin(response: HttpServletResponse) {
        val kakaoAuthUrl = "https://dev-solution.live/oauth2/authorization/kakao"
        response.sendRedirect(kakaoAuthUrl)
    }

    // 구글 리디렉트 URL
    @GetMapping("/google")
    fun googleLogin(response: HttpServletResponse) {
        val googleAuthUrl = "https://dev-solution.live/oauth2/authorization/google"
        response.sendRedirect(googleAuthUrl)
    }

    // 사용자 정보 호출
    @GetMapping("/info")
    fun getUserInfo(@RequestHeader("Authorization") token: String): ResponseEntity<ResponseUserInfoDTO> {
        val jwtToken = token.substring(7)
        val userInfo = userApiService.getUserInfoFromToken(jwtToken)
        return ResponseEntity.ok(userInfo)
    }

    // 닉네임 중복 확인
    @GetMapping("/duplicatecheck")
    fun checkNickname(@RequestParam nickname: String): ResponseEntity<Boolean> {
        val isDuplicate = userApiService.isNicknameDuplicate(nickname)
        return ResponseEntity.ok (isDuplicate)
    }

    // 사용자 정보 업데이트
    @PatchMapping("/update")
    fun signUp(@RequestHeader("Authorization") token: String, @RequestBody requestUserInfoDTO: RequestUserInfoDTO): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val result = userApiService.updateUserInfo(jwtToken, requestUserInfoDTO)
        println("request")
        return when (result) {
            0 -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("탈퇴한 지 7일 이내에는 다시 가입할 수 없습니다.")
            1, 2 -> ResponseEntity.status(HttpStatus.OK).body("사용자 정보 수정 성공")
            else -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 정보 수정 실패")
        }
    }

    // 토큰 검증
    @GetMapping("/validate")
    fun validateUserInfo(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        val isValid = jwtUtil.validateToken(jwtToken)
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.")
        }
        return ResponseEntity.ok("사용자 정보가 유효합니다.")
    }

    // 사용자 삭제
    @DeleteMapping("/delete")
    fun deleteUser(@RequestHeader("Authorization") token: String): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        userApiService.deleteUser(jwtToken)
        return ResponseEntity.ok("사용자 삭제 성공")
    }

    // 디바이스 ID 등록
    @PatchMapping("/registerdevice")
    fun registeredDeviceId(@RequestHeader("Authorization") token: String, @RequestBody deviceIdDTO: DeviceIdDTO): ResponseEntity<String> {
        val jwtToken = token.substring(7)
        return try {
            userApiService.registerDeviceId(jwtToken, deviceIdDTO.deviceId)
            ResponseEntity.ok("디바이스 값 추가 완료")
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("디바이스 값 추가 실패: ${e.message}, deviceId: ${deviceIdDTO.deviceId}")
        }
    }
}

