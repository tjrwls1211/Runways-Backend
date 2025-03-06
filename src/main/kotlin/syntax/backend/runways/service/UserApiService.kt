package syntax.backend.runways.service

import syntax.backend.runways.entity.User

interface UserApiService {
    fun getUserByEmail(email: String): User?
}