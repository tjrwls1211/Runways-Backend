package syntax.backend.runways.service

import org.springframework.stereotype.Service
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.UserApiRepository

@Service
class UserApiServiceImpl( private val userApiRepository: UserApiRepository ) : UserApiService {

    override fun getUserByEmail(email: String): User? {
        return userApiRepository.findByEmail(email)
    }

}