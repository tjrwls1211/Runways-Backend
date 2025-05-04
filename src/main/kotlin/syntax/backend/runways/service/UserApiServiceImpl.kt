package syntax.backend.runways.service

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import syntax.backend.runways.dto.*
import syntax.backend.runways.entity.Follow
import syntax.backend.runways.entity.User
import syntax.backend.runways.repository.UserRepository
import syntax.backend.runways.util.JwtUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class UserApiServiceImpl(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val courseQueryService: CourseQueryService
) : UserApiService {

    // 토큰에서 유저 정보 가져오기
    override fun getUserDataFromToken(token: String): User {
        val id = jwtUtil.extractUsername(token)
        val user: Optional<User> = userRepository.findById(id)
        if (user.isPresent) {
            return user.get()
        } else {
            throw Exception("User not found")
        }
    }

    // 아이디로 유저 정보 가져오기
    override fun getUserDataFromId(userId : String): User {
        val user: Optional<User> = userRepository.findById(userId)
        if (user.isPresent) {
            return user.get()
        } else {
            throw Exception("User not found")
        }
    }

    // 본인 정보 반환
    override fun getUserInfoFromUserId(userId : String, pageable: Pageable): ResponseMyInfoDTO {
        val user: Optional<User> = userRepository.findById(userId)
        if (user.isPresent) {
            val userInfo = user.get()
            return ResponseMyInfoDTO(
                id = userInfo.id,
                name = userInfo.name,
                email = userInfo.email,
                platform = userInfo.platform,
                profileImage = userInfo.profileImageUrl,
                birthDate = userInfo.birthdate,
                gender = userInfo.gender,
                nickname = userInfo.nickname,
                follow = userInfo.follow,
                marketing = userInfo.marketing,
                accountPrivate = userInfo.accountPrivate,
                courses = courseQueryService.getCourseList(userId, pageable, false),
                experience = userInfo.experience * 0.1f,
            )
        } else {
            throw EntityNotFoundException("User not found")
        }
    }

    // ID로 사용자 정보 반환
    override fun getUserInfoFromId(senderId : String, receiverId: String, pageable: Pageable): UserProfileWithCoursesDTO {
        val user = userRepository.findById(receiverId).orElseThrow { EntityNotFoundException("User not found") }
        val isFollowing = user.follow.isFollower(senderId)
        val courses = if (user.accountPrivate) {
            Page.empty(pageable)
        } else {
            courseQueryService.getCourseList(receiverId, pageable, true)
        }
        return UserProfileWithCoursesDTO(
            profileImage = user.profileImageUrl,
            nickname = user.nickname,
            follow = user.follow,
            accountPrivate = user.accountPrivate,
            courses = courses,
            isFollow = isFollowing,
            experience = user.experience * 0.1f,
        )
    }

    // 사용자 정보 업데이트
    override fun updateUserInfo(userId: String, requestUserInfoDTO: RequestUserInfoDTO): Int {
        val existingUser = userRepository.findById(userId)

        // 사용자가 존재하면 업데이트
        if (existingUser.isPresent) {
            val updatedUser = existingUser.get()

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
                userRepository.save(updatedUser)
                return 1
            } else {
                // 신규 가입자
                updatedUser.nickname = requestUserInfoDTO.nickname
                updatedUser.gender = requestUserInfoDTO.gender
                updatedUser.birthdate = requestUserInfoDTO.birthDate
                updatedUser.updatedAt = LocalDateTime.now()
                updatedUser.marketing = requestUserInfoDTO.marketing
                updatedUser.accountPrivate = requestUserInfoDTO.accountPrivate
                userRepository.save(updatedUser)
                return 2
            }
        } else {
            throw EntityNotFoundException("사용자를 찾을 수 없습니다.")
        }
    }

    // 닉네임 중복 확인
    override fun isNicknameDuplicate(nickname: String): Boolean {
        return !userRepository.existsByNickname(nickname)
    }

    // 사용자 삭제
    override fun deleteUser(userId: String) {
        val deleteUser = userRepository.findById(userId)
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
            userRepository.save(withdrawalUser)
        } else {
            throw Exception("User not found")
        }
    }

    // 디바이스 ID 업데이트
    override fun registerDeviceId(userId: String, deviceId:String) {
        val existingUser = userRepository.findById(userId)

        var cleanedDeviceId = deviceId
        if (cleanedDeviceId.contains("{") || cleanedDeviceId.contains("}")) {
            cleanedDeviceId = cleanedDeviceId.replace("{", "[").replace("}", "]")
        }

        // 사용자가 존재하면 업데이트
        if (existingUser.isPresent) {
            val updatedUser = existingUser.get()
            updatedUser.updatedAt = LocalDateTime.now()
            updatedUser.device = cleanedDeviceId
            userRepository.save(updatedUser)
        } else {
            throw Exception("User not found")
        }
    }

    // 팔로우 추가
    @Transactional
    override fun addFollow(senderId: String, receiverId: String) {
        // 팔로우 요청을 보낸 사용자 조회
        val senderUser = userRepository.findById(senderId)
            .orElseThrow { EntityNotFoundException("팔로우 요청을 보낸 사용자를 찾을 수 없습니다.") }

        // 팔로우 대상 사용자 조회
        val receiverUser = userRepository.findById(receiverId)
            .orElseThrow { EntityNotFoundException("팔로우 대상 사용자를 찾을 수 없습니다.") }

        // 중복 팔로우 방지
        if (receiverId in senderUser.follow.followings) {
            throw IllegalStateException("이미 팔로우한 사용자입니다.")
        }

        // 팔로잉 추가
        senderUser.follow.addFollowing(receiverId)
        userRepository.save(senderUser)

        // 팔로워 추가
        receiverUser.follow.addFollower(senderId)
        userRepository.save(receiverUser)
    }

    // 팔로워 목록 조회
    override fun getFollowerList(userId : String): List<FollowProfileDTO> {
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            val userInfo = user.get()
            val followerIds = userInfo.follow.followers
            val followers = userRepository.findByIdIn(followerIds)
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
        val user = userRepository.findById(userId)
        if (user.isPresent) {
            val userInfo = user.get()
            val followingIds = userInfo.follow.followings
            val followings = userRepository.findByIdIn(followingIds)
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

    // 팔로우 취소
    @Transactional
    override fun removeFollowing(senderId: String, receiverId: String) {
        // 팔로우 요청을 보낸 사용자 조회
        val senderUser = userRepository.findById(senderId)
            .orElseThrow { EntityNotFoundException("팔로우 요청을 보낸 사용자를 찾을 수 없습니다.") }

        // 팔로우 대상 사용자 조회
        val receiverUser = userRepository.findById(receiverId)
            .orElseThrow { EntityNotFoundException("팔로우 대상 사용자를 찾을 수 없습니다.") }

        // 팔로잉 목록에서 제거
        if (receiverId in senderUser.follow.followings) {
            senderUser.follow.removeFollowing(receiverId)
            userRepository.save(senderUser)
        } else {
            throw IllegalStateException("팔로우하지 않은 사용자입니다.")
        }

        // 팔로워 목록에서 제거
        if (senderId in receiverUser.follow.followers) {
            receiverUser.follow.removeFollower(senderId)
            userRepository.save(receiverUser)
        }
    }

    // 팔로워 삭제
    @Transactional
    override fun removeFollower(senderId: String, receiverId: String) {
        // 팔로우 요청을 보낸 사용자 조회
        val senderUser = userRepository.findById(senderId)
            .orElseThrow { EntityNotFoundException("팔로우 요청을 보낸 사용자를 찾을 수 없습니다.") }

        // 팔로우 대상 사용자 조회
        val receiverUser = userRepository.findById(receiverId)
            .orElseThrow { EntityNotFoundException("팔로우 대상 사용자를 찾을 수 없습니다.") }

        // 팔로잉 목록에서 제거
        if (senderId in receiverUser.follow.followings) {
            receiverUser.follow.removeFollowing(senderId)
            userRepository.save(receiverUser)
        } else {
            throw IllegalStateException("팔로우하지 않은 사용자입니다.")
        }

        // 팔로워 목록에서 제거
        if (receiverId in senderUser.follow.followers) {
            senderUser.follow.removeFollower(receiverId)
            userRepository.save(senderUser)
        }
    }

    // 랭킹 조회
    override fun getRankingList(pageable: Pageable): Page<UserRankingDTO> {
        val users = userRepository.findAllByRoleAndNicknameIsNotNullOrderByExperienceDesc("ROLE_USER", pageable)
        return users.map { user ->
            UserRankingDTO(
                id = user.id,
                nickname = if (user.accountPrivate) "비공개" else user.nickname,
                profileImage = user.profileImageUrl,
                experience = user.experience * 0.1f
            )
        }
    }
}