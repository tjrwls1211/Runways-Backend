package syntax.backend.runways.service

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.UserRepository
import java.util.*
import kotlin.collections.ArrayList

@Service
class UserDetailService(private val userRepository: UserRepository) : UserDetailsService {

    // 유저 필터 추가
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(id: String): UserDetails {
        val userDataOptional : Optional<User> = userRepository.findById(id)

        val userData: User = userDataOptional
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다 : $id") }

        // 권한 리스트 생성 및 추가
        val authorities: MutableList<GrantedAuthority> = ArrayList()
        authorities.add(SimpleGrantedAuthority(userData.role.name))

        // UserDetails 객체 반환
        return org.springframework.security.core.userdetails.User(userData.id, "", authorities)
    }
}