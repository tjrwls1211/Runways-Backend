package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.dto.UserInfoDTO
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.UserApiRepository
import syntax.backend.runways.util.JwtUtil
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
            return UserInfoDTO(userInfo.name, userInfo.email, userInfo.platform)
        } else {
            throw Exception("User not found")
        }
    }

}