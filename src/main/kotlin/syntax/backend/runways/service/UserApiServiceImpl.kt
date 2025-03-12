package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RequestUserInfoDTO
import syntax.backend.runways.dto.ResponseUserInfoDTO
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.UserApiRepository
import syntax.backend.runways.util.JwtUtil
import java.time.LocalDateTime
import java.util.*

@Service
class UserApiServiceImpl(
    private val userApiRepository: UserApiRepository,
    private val jwtUtil: JwtUtil
) : UserApiService {

    override fun getUserDataFromToken(token: String): User {
        val id = jwtUtil.extractUsername(token)
        val user: Optional<User> = userApiRepository.findById(id)
        if (user.isPresent) {
            return user.get()
        } else {
            throw Exception("User not found")
        }
    }

    // 토큰으로 사용자 정보 반환
    override fun getUserInfoFromToken(token: String): ResponseUserInfoDTO {
        val id = jwtUtil.extractUsername(token)
        val user: Optional<User> = userApiRepository.findById(id)
        if (user.isPresent) {
            val userInfo = user.get()
            return ResponseUserInfoDTO(userInfo.name, userInfo.email, userInfo.platform, userInfo.profileImageUrl,userInfo.birthdate, userInfo.gender, userInfo.nickname)
        } else {
            throw Exception("User not found")
        }
    }

    // TODO : 테스트 필요
    // 사용자 정보 업데이트
    override fun updateUserInfo(token: String, requestUserInfoDTO: RequestUserInfoDTO): User {
        val id = jwtUtil.extractUsername(token)
        val existingUser = userApiRepository.findById(id)
        // 사용자가 존재하면
        if (existingUser.isPresent) {
            val updatedUser = existingUser.get()
            updatedUser.nickname = requestUserInfoDTO.nickname
            updatedUser.gender = requestUserInfoDTO.gender
            updatedUser.birthdate = requestUserInfoDTO.birthDate
            updatedUser.updatedAt = LocalDateTime.now()
            return userApiRepository.save(updatedUser)
        } else {
            throw Exception("User not found")
        }
    }

    // 닉네임 중복 확인
    override fun isNicknameDuplicate(nickname: String): Boolean {
        return !userApiRepository.existsByNickname(nickname)
    }
}