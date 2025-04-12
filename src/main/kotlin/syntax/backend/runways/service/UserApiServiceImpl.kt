package syntax.backend.runways.service

import jakarta.persistence.Entity
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import syntax.backend.runways.dto.RequestUserInfoDTO
import syntax.backend.runways.dto.ResponseCourseDTO
import syntax.backend.runways.dto.ResponseMyInfoDTO
import syntax.backend.runways.dto.UserProfileWithCoursesDTO
import syntax.backend.runways.entity.Follow
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.CourseApiRepository
import syntax.backend.runways.repository.UserApiRepository
import syntax.backend.runways.util.JwtUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class UserApiServiceImpl(
    private val userApiRepository: UserApiRepository,
    private val jwtUtil: JwtUtil,
    private val courseQueryService: CourseQueryService
) : UserApiService {

    // 토큰에서 유저 정보 가져오기
    override fun getUserDataFromToken(token: String): User {
        val id = jwtUtil.extractUsername(token)
        val user: Optional<User> = userApiRepository.findById(id)
        if (user.isPresent) {
            return user.get()
        } else {
            throw Exception("User not found")
        }
    }

    // 토큰으로 사용자 정보 반환
    override fun getUserInfoFromToken(token: String): ResponseMyInfoDTO {
        val id = jwtUtil.extractUsername(token)
        val user: Optional<User> = userApiRepository.findById(id)
        if (user.isPresent) {
            val userInfo = user.get()
            return ResponseMyInfoDTO(
                userInfo.name,
                userInfo.email,
                userInfo.platform,
                userInfo.profileImageUrl,
                userInfo.birthdate,
                userInfo.gender,
                userInfo.nickname,
                userInfo.follow,
                userInfo.follow.followers,
                userInfo.follow.following,
                userInfo.marketing,
                userInfo.accountPrivate
                )
        } else {
            throw EntityNotFoundException("User not found")
        }
    }

    // ID로 사용자 정보 반환
    override fun getUserInfoFromId(userId: String, pageable: Pageable): UserProfileWithCoursesDTO {
        val user = userApiRepository.findById(userId).orElseThrow { EntityNotFoundException("User not found") }
        val courses = courseQueryService.getCourseList(userId, pageable, true)

        println("courses : ${courses.content}")


        return UserProfileWithCoursesDTO(
            profileImage = user.profileImageUrl,
            nickname = user.nickname,
            followers = emptyList(),
            following = emptyList(),
            accountPrivate = user.accountPrivate,
            courses = courses
        )
    }

    // 사용자 정보 업데이트
    override fun updateUserInfo(token: String, requestUserInfoDTO: RequestUserInfoDTO): Int {
        val id = jwtUtil.extractUsername(token)
        val existingUser = userApiRepository.findById(id)

        // 사용자가 존재하면 업데이트
        if (existingUser.isPresent) {
            val updatedUser = existingUser.get()
            println("Updated User: $updatedUser")

            // 탈퇴한지 7일 이내인 경우
            if (updatedUser.role == "ROLE_WITHDRAWAL" && updatedUser.updatedAt > LocalDateTime.now().minusDays(7)) {
                return 0
            } else if (updatedUser.role == "ROLE_WITHDRAWAL" && updatedUser.updatedAt < LocalDateTime.now().minusDays(7)) {
                // 탈퇴한지 7일이 경과한 경우
                updatedUser.nickname = requestUserInfoDTO.nickname
                updatedUser.gender = requestUserInfoDTO.gender
                updatedUser.birthdate = requestUserInfoDTO.birthDate
                updatedUser.role = "ROLE_USER"
                updatedUser.updatedAt = LocalDateTime.now()
                updatedUser.marketing = requestUserInfoDTO.marketing
                updatedUser.accountPrivate = requestUserInfoDTO.accountPrivate
                userApiRepository.save(updatedUser)
                return 1
            } else {
                // 신규 가입자
                updatedUser.nickname = requestUserInfoDTO.nickname
                updatedUser.gender = requestUserInfoDTO.gender
                updatedUser.birthdate = requestUserInfoDTO.birthDate
                updatedUser.updatedAt = LocalDateTime.now()
                updatedUser.marketing = requestUserInfoDTO.marketing
                userApiRepository.save(updatedUser)
                return 2
            }
        } else {
            throw EntityNotFoundException("사용자를 찾을 수 없습니다.")
        }
    }

    // 닉네임 중복 확인
    override fun isNicknameDuplicate(nickname: String): Boolean {
        return !userApiRepository.existsByNickname(nickname)
    }

    // 사용자 삭제
    override fun deleteUser(token: String) {
        val id = jwtUtil.extractUsername(token)
        val deleteUser = userApiRepository.findById(id)
        if (deleteUser.isPresent) {
            val withdrawalUser = deleteUser.get()
            withdrawalUser.name = null
            withdrawalUser.birthdate = LocalDate.now()
            withdrawalUser.email = null
            withdrawalUser.platform = ""
            withdrawalUser.nickname = null
            withdrawalUser.gender = null
            withdrawalUser.follow = Follow()
            withdrawalUser.role = "ROLE_WITHDRAWAL"
            withdrawalUser.profileImageUrl = null
            withdrawalUser.updatedAt = LocalDateTime.now()
            withdrawalUser.marketing = false
            withdrawalUser.device = null
            userApiRepository.save(withdrawalUser)
        } else {
            throw Exception("User not found")
        }
    }

    // 디바이스 ID 업데이트
    override fun registerDeviceId(token: String, deviceId:String) {
        val id = jwtUtil.extractUsername(token)
        val existingUser = userApiRepository.findById(id)

        var cleanedDeviceId = deviceId
        if (cleanedDeviceId.contains("{") || cleanedDeviceId.contains("}")) {
            cleanedDeviceId = cleanedDeviceId.replace("{", "[").replace("}", "]")
        }

        // 사용자가 존재하면 업데이트
        if (existingUser.isPresent) {
            val updatedUser = existingUser.get()
            updatedUser.updatedAt = LocalDateTime.now()
            updatedUser.device = cleanedDeviceId
            userApiRepository.save(updatedUser)
        } else {
            throw Exception("User not found")
        }
    }

    // 팔로워 목록 조회
    override fun getFollowerList(token: String): List<String> {
        val id = jwtUtil.extractUsername(token)
        val user: Optional<User> = userApiRepository.findById(id)
        if (user.isPresent) {
            val userInfo = user.get()
            return userInfo.follow.followers
        } else {
            throw EntityNotFoundException("User not found")
        }
    }

    // 팔로잉 목록 조회
    override fun getFollowingList(token: String): List<String> {
        val id = jwtUtil.extractUsername(token)
        val user: Optional<User> = userApiRepository.findById(id)
        if (user.isPresent) {
            val userInfo = user.get()
            return userInfo.follow.following
        } else {
            throw EntityNotFoundException("User not found")
        }
    }
}