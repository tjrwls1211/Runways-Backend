package syntax.backend.runity.service

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import syntax.backend.runity.entity.User
import syntax.backend.runity.repository.UserApiRepository
import java.util.*
import kotlin.collections.ArrayList

@Service
class UserApiServiceImpl( private val userApiRepository: UserApiRepository ) : UserApiService {

    override fun getUserByEmail(email: String): User? {
        return userApiRepository.findByEmail(email)
    }

}