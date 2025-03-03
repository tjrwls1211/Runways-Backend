package syntax.backend.runity.config.oauth

import lombok.RequiredArgsConstructor
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import syntax.backend.runity.entity.User
import syntax.backend.runity.repository.UserApiRepository

@RequiredArgsConstructor
@Service
class OAuth2UserCustomService(private val userApiRepository: UserApiRepository) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val attributes = oAuth2User.attributes

        val userInfo = User(
            id = attributes["sub"] as String,
            name = attributes["name"] as String,
            email = attributes["email"] as String,
        )

        println("로그인한 사용자: $userInfo")
        saveOrUpdateUser(userInfo)
        return oAuth2User
    }

    fun saveOrUpdateUser(user: User) {
        val existingUser = userApiRepository.findById(user.id)
        if (existingUser.isPresent) {
            val updatedUser = existingUser.get().apply {
                name = user.name
                email = user.email
            }
            userApiRepository.save(updatedUser)
        } else {
            userApiRepository.save(user)
        }
    }
}