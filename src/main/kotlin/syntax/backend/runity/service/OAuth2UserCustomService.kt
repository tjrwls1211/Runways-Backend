package syntax.backend.runity.service

import lombok.RequiredArgsConstructor
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import syntax.backend.runity.entity.User
import syntax.backend.runity.repository.UserApiRepository

@RequiredArgsConstructor
@Service
class OAuth2UserCustomService(private val userApiRepository: UserApiRepository) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val attributes = oAuth2User.attributes.toMutableMap()
        val registrationId = userRequest.clientRegistration.registrationId

        val userInfo = when (registrationId) {
            "google" -> {
                User(
                    id = attributes["sub"] as String,
                    name = attributes["name"] as String,
                    email = attributes["email"] as String,
                    platform = registrationId
                )
            }
            "kakao" -> {
                val kakaoAccount = attributes["kakao_account"] as? Map<String, Any> ?: throw IllegalArgumentException("Kakao account not found")
                val profile = kakaoAccount["profile"] as? Map<String, Any> ?: throw IllegalArgumentException("Kakao profile not found")

                User(
                    id = attributes["id"].toString(),
                    name = profile["nickname"] as String,
                    email = kakaoAccount["email"] as String,
                    platform = registrationId
                )
            }
            else -> throw IllegalArgumentException("Unsupported provider: $registrationId")
        }

        println("로그인한 사용자: $userInfo")
        saveOrUpdateUser(userInfo)

        val nonNullName = userInfo.name
        attributes["name"] = nonNullName

        return DefaultOAuth2User(oAuth2User.authorities, attributes, "name")
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