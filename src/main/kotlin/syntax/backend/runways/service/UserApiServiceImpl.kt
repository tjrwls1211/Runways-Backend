package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.UserInfoDTO
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

    // 토큰으로 사용자 정보 반환
    override fun getUserInfoFromToken(token: String): UserInfoDTO {
        val id = jwtUtil.extractUsername(token)
        val user: Optional<User> = userApiRepository.findById(id)
        if (user.isPresent) {
            val userInfo = user.get()
            return UserInfoDTO(userInfo.name, userInfo.email, userInfo.platform, userInfo.profileImageUrl,userInfo.birthdate, userInfo.gender, userInfo.nickname, )
        } else {
            throw Exception("User not found")
        }
    }

    // TODO : 테스트 필요
    // 사용자 정보 업데이트
    override fun updateUserInfo(token: String, userInfoDTO: UserInfoDTO): User {
        val id = jwtUtil.extractUsername(token)
        val existingUser = userApiRepository.findById(id)
        if (existingUser.isPresent) {
            val updatedUser = existingUser.get()
            updatedUser.nickname = userInfoDTO.nickname
            updatedUser.gender = userInfoDTO.gender
            updatedUser.birthdate = userInfoDTO.birthDate
            updatedUser.updatedAt = LocalDateTime.now()
            return userApiRepository.save(updatedUser)
        } else {
            throw Exception("User not found")
        }
    }

    // 닉네임 중복 확인
    override fun isNicknameDuplicate(nickname: String): Boolean {
        return userApiRepository.existsByNickname(nickname)
    }
}