package syntax.backend.runity.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Repository
import syntax.backend.runity.entity.User


@Repository
interface UserApiRepository : JpaRepository<User, String>{
    fun findByEmail(email: String): User?

}