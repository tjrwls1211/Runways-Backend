package syntax.backend.runways.service

import syntax.backend.runways.dto.UserInfoDTO
import syntax.backend.runways.entity.User

interface UserApiService {
    fun getUserInfoFromToken(token: String): UserInfoDTO
}