package syntax.backend.runways.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import syntax.backend.runways.dto.*
import syntax.backend.runways.entity.Follow
import syntax.backend.runways.entity.User
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
        val courses = if (user.accountPrivate) {
            Page.empty(pageable)
        } else {
            courseQueryService.getCourseList(userId, pageable, true)
        }
        return UserProfileWithCoursesDTO(
            profileImage = user.profileImageUrl,
            nickname = user.nickname,
            follow = user.follow,
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

    // 팔로우 추가
    @Transactional
    override fun addFollow(senderId: String, receiverId: String) {
        // 팔로우 요청을 보낸 사용자 조회
        val senderUser = userApiRepository.findById(senderId)
            .orElseThrow { EntityNotFoundException("팔로우 요청을 보낸 사용자를 찾을 수 없습니다.") }

        // 팔로우 대상 사용자 조회
        val receiverUser = userApiRepository.findById(receiverId)
            .orElseThrow { EntityNotFoundException("팔로우 대상 사용자를 찾을 수 없습니다.") }

        // 중복 팔로우 방지
        if (receiverId in senderUser.follow.followings) {
            throw IllegalStateException("이미 팔로우한 사용자입니다.")
        }

        // 팔로잉 추가
        senderUser.follow.addFollowing(receiverId)
        userApiRepository.save(senderUser)

        // 팔로워 추가
        receiverUser.follow.addFollower(senderId)
        userApiRepository.save(receiverUser)
    }

    // 팔로워 목록 조회
    override fun getFollowerList(userId : String): List<FollowProfileDTO> {
        val user = userApiRepository.findById(userId)
        if (user.isPresent) {
            val userInfo = user.get()
            val followerIds = userInfo.follow.followers
            val followers = userApiRepository.findByIdIn(followerIds)
            return followers.map { follower ->
                FollowProfileDTO(
                    id = follower.id,
                    nickname = follower.nickname,
                    profileImage = follower.profileImageUrl,
                )
            }
        } else {
            throw EntityNotFoundException("사용자를 찾을 수 없습니다.")
        }
    }

    // 팔로잉 목록 조회
    override fun getFollowingList(userId:String): List<FollowProfileDTO> {
        val user = userApiRepository.findById(userId)
        if (user.isPresent) {
            val userInfo = user.get()
            val followingIds = userInfo.follow.followings
            val followings = userApiRepository.findByIdIn(followingIds)
            return followings.map { following ->
                FollowProfileDTO(
                    id = following.id,
                    nickname = following.nickname,
                    profileImage = following.profileImageUrl,
                )
            }
        } else {
            throw EntityNotFoundException("사용자를 찾을 수 없습니다.")
        }
    }

    @Transactional
    override fun removeFollow(senderId: String, receiverId: String) {
        // 팔로우 요청을 보낸 사용자 조회
        val senderUser = userApiRepository.findById(senderId)
            .orElseThrow { EntityNotFoundException("팔로우 요청을 보낸 사용자를 찾을 수 없습니다.") }

        // 팔로우 대상 사용자 조회
        val receiverUser = userApiRepository.findById(receiverId)
            .orElseThrow { EntityNotFoundException("팔로우 대상 사용자를 찾을 수 없습니다.") }

        // 팔로잉 목록에서 제거
        if (receiverId in senderUser.follow.followings) {
            senderUser.follow.removeFollowing(receiverId)
            userApiRepository.save(senderUser)
        } else {
            throw IllegalStateException("팔로우하지 않은 사용자입니다.")
        }

        // 팔로워 목록에서 제거
        if (senderId in receiverUser.follow.followers) {
            receiverUser.follow.removeFollower(senderId)
            userApiRepository.save(receiverUser)
        }
    }
}