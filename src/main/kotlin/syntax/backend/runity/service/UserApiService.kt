package syntax.backend.runity.service

import syntax.backend.runity.entity.User

interface UserApiService {
    fun getUserByEmail(email: String): User?
    fun getGoogleUserInfo(accessToken: String): User
    fun getKakaoUserInfo(accessToken: String): User
    fun saveOrUpdateUser(user: User): User
}