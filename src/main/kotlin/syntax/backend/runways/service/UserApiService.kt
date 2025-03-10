package syntax.backend.runways.service

import syntax.backend.runways.dto.RequestUserInfoDTO
import syntax.backend.runways.dto.ResponseUserInfoDTO
import syntax.backend.runways.entity.User

interface UserApiService {
    fun getUserDataFromToken(token: String): User
    fun getUserInfoFromToken(token: String): ResponseUserInfoDTO
    fun updateUserInfo(token: String, requestUserInfoDTO: RequestUserInfoDTO): User
    fun isNicknameDuplicate(nickname: String): Boolean
}