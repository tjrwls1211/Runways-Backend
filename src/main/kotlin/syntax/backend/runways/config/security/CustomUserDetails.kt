package syntax.backend.runways.config.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import syntax.backend.runways.entity.User

class CustomUserDetails(private val user: User) : UserDetails {
    override fun getAuthorities() = listOf(SimpleGrantedAuthority(user.role.name))
    override fun getUsername() = user.id
    override fun getPassword() = "" // 비번 안 쓰면 이렇게
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true

    fun getUser(): User = user
}
