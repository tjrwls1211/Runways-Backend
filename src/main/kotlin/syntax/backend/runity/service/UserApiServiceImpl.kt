package syntax.backend.runity.service

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import syntax.backend.runity.entity.User
import syntax.backend.runity.repository.UserApiRepository

@Service
class UserApiServiceImpl(private val userApiRepository: UserApiRepository) : UserApiService {

    // 이메일로 조회
    override fun getUserByEmail(email: String): User? {
        return userApiRepository.findByEmail(email)
    }

    // 구글 간편 로그인 시 사용자 정보 조회
    override fun getGoogleUserInfo(accessToken: String): User {
        val restTemplate = RestTemplate()
        val userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo"

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }
        val entity = HttpEntity<String>(headers)
        val response = restTemplate.exchange(userInfoEndpoint, HttpMethod.GET, entity, Map::class.java)

        val attributes = response.body ?: throw IllegalArgumentException("Failed to fetch user info")

        return User(
            id = attributes["sub"] as String,
            name = attributes["name"] as String,
            email = attributes["email"] as String,
            platform = "google"
        )
    }

    // 카카오 간편 로그인 시 사용자 정보 조회
    override fun getKakaoUserInfo(accessToken: String): User {
        val restTemplate = RestTemplate()
        val userInfoEndpoint = "https://kapi.kakao.com/v2/user/me"

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }
        val entity = HttpEntity<String>(headers)
        val response = restTemplate.exchange(userInfoEndpoint, HttpMethod.GET, entity, Map::class.java)

        val attributes = response.body ?: throw IllegalArgumentException("Failed to fetch user info")
        val kakaoAccount = attributes["kakao_account"] as? Map<String, Any> ?: throw IllegalArgumentException("Kakao account not found")
        val profile = kakaoAccount["profile"] as? Map<String, Any> ?: throw IllegalArgumentException("Kakao profile not found")

        return User(
            id = attributes["id"].toString(),
            name = profile["nickname"] as String,
            email = kakaoAccount["email"] as String,
            platform = "kakao"
        )
    }

    // 로그인 시 사용자 저장 및 업데이트
    override fun saveOrUpdateUser(user: User): User {
        val existingUser = userApiRepository.findById(user.id)
        return if (existingUser.isPresent) {
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