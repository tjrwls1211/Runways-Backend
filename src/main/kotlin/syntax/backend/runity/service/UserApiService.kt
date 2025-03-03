package syntax.backend.runity.service

import syntax.backend.runity.entity.User

interface UserApiService {
    fun getUserByEmail(email: String): User?
}