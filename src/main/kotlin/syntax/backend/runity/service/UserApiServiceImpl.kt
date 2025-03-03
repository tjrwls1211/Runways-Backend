package syntax.backend.runity.service

import org.springframework.stereotype.Service
import syntax.backend.runity.entity.User
import syntax.backend.runity.repository.UserApiRepository

@Service
class UserApiServiceImpl(
    private val userApiRepository: UserApiRepository
) : UserApiService {

    override fun getUserByEmail(email: String): User? {
        return userApiRepository.findByEmail(email)
    }
}