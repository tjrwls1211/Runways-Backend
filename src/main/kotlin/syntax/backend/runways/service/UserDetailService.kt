package syntax.backend.runways.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import syntax.backend.runways.config.security.CustomUserDetails
import syntax.backend.runways.repository.UserRepository

@Service
class UserDetailService(private val userRepository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(id: String): UserDetails {
        val user = userRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다 : $id") }

        return CustomUserDetails(user)
    }
}
