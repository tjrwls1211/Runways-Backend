package syntax.backend.runity.service

import org.springframework.security.core.userdetails.UserDetails
import syntax.backend.runity.entity.User

interface UserApiService {
    fun getUserByEmail(email: String): User?
}