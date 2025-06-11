package syntax.backend.runways.service

import lombok.RequiredArgsConstructor
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import syntax.backend.runways.entity.Follow
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.UserRepository
import java.time.LocalDate

@RequiredArgsConstructor
@Service
class OAuth2UserCustomService(private val userRepository: UserRepository) : DefaultOAuth2UserService() {

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
                    platform = registrationId,
                    profileImageUrl = attributes["picture"] as String,
                    follow = Follow(),
                    gender = null,
                    birthdate = LocalDate.now(),
                    nickname = null
                )
            }
            "kakao" -> {
                val kakaoAccount = attributes["kakao_account"] as? Map<String, Any> ?: throw IllegalArgumentException("Kakao account not found")
                val profile = kakaoAccount["profile"] as? Map<String, Any> ?: throw IllegalArgumentException("Kakao profile not found")

                User(
                    id = attributes["id"].toString(),
                    name = profile["nickname"] as String,
                    email = kakaoAccount["email"] as String,
                    platform = registrationId,
                    profileImageUrl = profile["profile_image_url"] as String,
                    follow = Follow(),
                    gender = null,
                    birthdate = LocalDate.now(),
                    nickname = null
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
        val existingUser = userRepository.findById(user.id)
        if (existingUser.isPresent) {
            val updatedUser = existingUser.get().apply {
                name = user.name
                email = user.email
                profileImageUrl = user.profileImageUrl
                platform = user.platform
            }
            userRepository.save(updatedUser)
        } else {
            userRepository.save(user)
        }
    }
}